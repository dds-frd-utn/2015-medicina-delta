package models

import play.api.libs.json.{JsValue, Json, Format}
import play.api.Play.current
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.Future

case class Paciente(
                     id: Long,
                     nombre: String,
                     apellido: String,
                     dni: Long,
                     obraSocial: Int
                     )

object Paciente {

  implicit val format: Format[Paciente] = Json.format[Paciente]
  protected val dbConfig = DatabaseConfigProvider.get[JdbcProfile](current)

  import dbConfig._
  import dbConfig.driver.api._

  class TablaPacientes(tag: Tag) extends Table[Paciente](tag, "PACIENTES") {

    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)

    def nombre = column[String]("NOMBRE")

    def apellido = column[String]("APELLIDO")

    def dni = column[Long]("DNI")

    def obrasocial = column[Int]("OBRASOCIAL")

    def * = (id, nombre, apellido, dni, obrasocial) <>((Paciente.apply _).tupled, Paciente.unapply _)
  }

  val tabla = TableQuery[TablaPacientes]

  def listar: Future[Seq[Paciente]] = {
    val listaDePacientes = tabla.result
    db.run(listaDePacientes)
  }

  def listarJSON: Future[JsValue] = {
    val listaDePacientes = tabla.result
    val movida = db.run(listaDePacientes)
    val movidaJSON = movida map { p => Json.toJson(p)}
    movidaJSON
  }

  def getByID(idPaciente: Long): Future[Option[Paciente]] = {
    val pacienteByID = tabla.filter { f => f.id === idPaciente }.
      result.headOption

    db.run(pacienteByID)
  }

  def create(paciente: Paciente): Future[Paciente] = {
    val insercion = (tabla returning tabla.map(_.id)) += paciente

    val insertedIDFuture = db.run(insercion)

    val copiaPaciente: Future[Paciente] = insertedIDFuture.map { nuevaID =>
      paciente.copy(id = nuevaID)
    }

    copiaPaciente
  }

}