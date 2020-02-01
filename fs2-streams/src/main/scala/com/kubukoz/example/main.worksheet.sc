//IO effect type
import cats.effect.IO

//syntax, e.g. mapN, *>
import cats.implicits._

//console4cats Console methods for IO
import cats.effect.Console.io._

//core fs2 abstraction
import fs2.Stream

//syntax for sleeping - e.g. 5.seconds
import scala.concurrent.duration._

//.showValues syntax for safe stream running in worksheets
import com.kubukoz.example.SnippetRunner._
