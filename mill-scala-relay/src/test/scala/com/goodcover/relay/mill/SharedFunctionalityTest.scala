package com.goodcover.relay.mill

import com.goodcover.relay.build.*
import utest.*

import java.io.File
import java.nio.file.Files
import scala.meta.dialects

object SharedFunctionalityTest extends TestSuite {

  def withTempDir[T](f: File => T): T = {
    val tempDir = Files.createTempDirectory("relay-test").toFile
    try f(tempDir)
    finally {
      def deleteRecursively(file: File): Unit = {
        if (file.isDirectory) {
          file.listFiles().foreach(deleteRecursively)
        }
        file.delete(): Unit
      }
      deleteRecursively(tempDir)
    }
  }

  def createTestScalaFile(dir: File): File = {
    val scalaContent =
      """package com.example
        |
        |import scala.scalajs.js
        |
        |object UserQueries {
        |
        |  @graphql("query GetUser($id: ID!) { user(id: $id) { id name email } }")
        |  def getUserQuery: js.Object = js.native
        |
        |  val withMacro = graphqlGen("mutation CreateUser($input: CreateUserInput!) { createUser(input: $input) { id name } }")
        |}
        |""".stripMargin

    val file = new File(dir, "UserQueries.scala")
    java.nio.file.Files.write(file.toPath, scalaContent.getBytes)
    file
  }

  def createTestSchema(dir: File): File = {
    val schemaContent =
      """type Query {
        |  user(id: ID!): User
        |}
        |
        |type User {
        |  id: ID!
        |  name: String!
        |  email: String
        |}
        |
        |type Mutation {
        |  createUser(input: CreateUserInput!): User
        |}
        |
        |input CreateUserInput {
        |  name: String!
        |  email: String
        |}
        |""".stripMargin

    val file = new File(dir, "schema.graphql")
    java.nio.file.Files.write(file.toPath, schemaContent.getBytes)
    file
  }

  val tests = Tests {

    test("GraphQLExtractor extracts GraphQL from Scala files") {
      withTempDir { tempDir =>
        val sourceDir = new File(tempDir, "src")
        val outputDir = new File(tempDir, "extracted")
        sourceDir.mkdirs()
        outputDir.mkdirs()

        val scalaFile = createTestScalaFile(sourceDir)



        val sourceFiles = Set(scalaFile)
        val options = GraphQLExtractor.Options(outputDir, dialects.Scala3)
        val logger = TestBuildLogger()

        val results = GraphQLExtractor.extractSimple(sourceFiles, options, logger)



        // Should extract GraphQL files
        assert(results.nonEmpty)

        // Check that GraphQL files were created
        val extractedFiles = outputDir.listFiles().filter(_.getName.endsWith(".graphql"))
        assert(extractedFiles.nonEmpty)

        // Check content of extracted files
        val extractedContent = extractedFiles.map(f =>
          new String(java.nio.file.Files.readAllBytes(f.toPath))
        ).mkString("\n")

        assert(extractedContent.contains("query GetUser"))
        assert(extractedContent.contains("mutation CreateUser"))
      }
    }

    test("GraphQLConverter processes GraphQL files") {
      withTempDir { tempDir =>
        val schemaDir = new File(tempDir, "schema")
        val graphqlDir = new File(tempDir, "graphql")
        val outputDir = new File(tempDir, "converted")
        schemaDir.mkdirs()
        graphqlDir.mkdirs()
        outputDir.mkdirs()

        val schemaFile = createTestSchema(schemaDir)

        // Create a simple GraphQL file
        val graphqlContent =
          """query GetUser($id: ID!) {
            |  user(id: $id) {
            |    id
            |    name
            |    email
            |  }
            |}
            |""".stripMargin
        val graphqlFile = new File(graphqlDir, "GetUser.graphql")
        java.nio.file.Files.write(graphqlFile.toPath, graphqlContent.getBytes)

        val graphqlFiles = Set(graphqlFile)
        val options = GraphQLConverter.Options(outputDir, Map.empty[String, String])
        val logger = TestBuildLogger()

        val results = GraphQLConverter.convertSimple(
          graphqlFiles,
          schemaFile,
          Set.empty,
          options,
          logger
        )

        // Should generate Scala files (even if placeholder)
        assert(results.nonEmpty)
      }
    }

    test("RelayCompiler handles options correctly") {
      withTempDir { tempDir =>
        val workingDir = new File(tempDir, "working")
        val sourceDir = new File(tempDir, "source")
        val outputDir = new File(tempDir, "output")
        val schemaFile = createTestSchema(tempDir)

        workingDir.mkdirs()
        sourceDir.mkdirs()
        outputDir.mkdirs()

        val options = RelayCompiler.Options(
          workingDir = workingDir,
          compilerCommand = "relay-compiler",
          schemaPath = schemaFile,
          sourceDirectory = sourceDir,
          outputPath = outputDir,
          verbose = false,
          includes = Seq("**/*.graphql"),
          excludes = Seq.empty,
          extensions = Seq("js"),
          persisted = None,
          customScalars = Map("DateTime" -> "js.Date"),
          displayOnFailure = false,
          typeScript = false
        )

        // Test options properties
        assert(options.language == "javascript")
        assert(options.customScalars.contains("DateTime"))
        assert(options.customScalars("DateTime") == "js.Date")

        // Test TypeScript option
        val tsOptions = options.copy(typeScript = true)
        assert(tsOptions.language == "typescript")
      }
    }

    test("RelayCompiler with mock process runner") {
      withTempDir { tempDir =>
        val workingDir = new File(tempDir, "working")
        val sourceDir = new File(tempDir, "source")
        val outputDir = new File(tempDir, "output")
        val schemaFile = createTestSchema(tempDir)

        workingDir.mkdirs()
        sourceDir.mkdirs()
        outputDir.mkdirs()

        val options = RelayCompiler.Options(
          workingDir = workingDir,
          compilerCommand = "relay-compiler",
          schemaPath = schemaFile,
          sourceDirectory = sourceDir,
          outputPath = outputDir,
          verbose = false,
          includes = Seq("**/*.graphql"),
          excludes = Seq.empty,
          extensions = Seq("js"),
          persisted = None,
          customScalars = Map.empty,
          displayOnFailure = false,
          typeScript = false
        )

        val logger = TestBuildLogger()
        val processRunner = TestProcessRunner()

        // Create some mock output files that the relay compiler would generate
        java.nio.file.Files.write(
          new File(outputDir, "UserQueries.js").toPath,
          "// Generated by relay-compiler".getBytes
        )

        val results = RelayCompiler.compileSimple(options, logger, processRunner)



        // Should handle compilation (even if mocked)
        assert(results.nonEmpty)
        assert(processRunner.commands.nonEmpty)
        assert(processRunner.lastCommand.exists(_.mkString(" ").contains("relay-compiler")))
      }
    }

    test("Clean operations work without errors") {
      withTempDir { tempDir =>
        val extractDir = new File(tempDir, "extract")
        val convertDir = new File(tempDir, "convert")
        val compileDir = new File(tempDir, "compile")

        extractDir.mkdirs()
        convertDir.mkdirs()
        compileDir.mkdirs()

        // Create some test files
        java.nio.file.Files.write(
          new File(extractDir, "test.graphql").toPath,
          "query Test { __typename }".getBytes
        )
        java.nio.file.Files.write(
          new File(convertDir, "Test.scala").toPath,
          "object Test".getBytes
        )
        java.nio.file.Files.write(
          new File(compileDir, "test.js").toPath,
          "// generated".getBytes
        )

        // Clean operations should not throw exceptions
        GraphQLExtractor.clean(extractDir)
        GraphQLConverter.clean(convertDir)
        RelayCompiler.clean(compileDir)

        // Test passes if no exceptions are thrown
        assert(true)
      }
    }
  }
}

// Test helper classes are imported from RelayIntegrationTest
