package demo

import cats.effect.ExitCode

import cats.effect.IO
import cats.effect.IOApp
import java.io.File
import java.io.FileReader
import cats.effect.Blocker
import cats.effect.Sync
import cats.Applicative
import cats.effect.Console
import cats.Monad
import java.io.BufferedReader
import cats.effect.ContextShift
import cats.implicits._
import cats.effect.Console.io.putStrLn

object ResourceDemo extends IOApp {
  val file1 = new File("src/main/resources/example.txt")
  def file2(name: String) = new File("src/main/resources/" + name)

  override def run(args: List[String]): IO[ExitCode] = {
    Blocker[IO].use { blocker =>
      val files = Files.fileSystem[IO](blocker)

      files
        .open(file1)
        .bracket { file1 =>
          files.readLine(file1)
        }(files.close)
        .flatMap(putStrLn)
    }
  }.as(ExitCode.Success)
}

trait Files[F[_]] {
  def open(file: File): F[BufferedReader]
  def readLine(reader: BufferedReader): F[String]
  def close(reader: BufferedReader): F[Unit]
}

object Files {
  def apply[F[_]](implicit F: Files[F]): Files[F] = F

  def fileSystem[F[_]: ContextShift: Sync](blocker: Blocker): Files[F] = new Files[F] {

    def open(file: File): F[BufferedReader] = blocker.delay[F, BufferedReader] {
      println("Opening file reader")
      new BufferedReader(new FileReader(file))
    }

    def readLine(reader: BufferedReader): F[String] = blocker.delay[F, String] {
      reader.readLine()
    }

    def close(reader: BufferedReader): F[Unit] = blocker.delay[F, Unit] {
      println("Closing file reader")
      reader.close()
    }
  }
}

trait BusinessService[F[_]] {
  def getBusinessData: F[List[String]]
}

object BusinessService {
  def apply[F[_]](implicit F: BusinessService[F]): BusinessService[F] = F

  def fromFiles[F[_]: Files: Applicative](readers: List[BufferedReader]): BusinessService[F] = new BusinessService[F] {
    val getBusinessData: F[List[String]] = readers.traverse(Files[F].readLine)
  }
}

trait App[F[_]] {
  def run: F[Unit]
}

object App {

  def instance[F[_]: BusinessService: Console: Monad]: App[F] = new App[F] {

    val run: F[Unit] = BusinessService[F]
      .getBusinessData
      .flatMap { results =>
        results.traverse(Console[F].putStrLn)
      }
      .void
  }
}
