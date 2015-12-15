package models

import java.util.UUID

import play.api.libs.json.{Json, Format}
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

case class DatosPaciente(nombre: String, apellido: String, dni: Long, obrasocial: Int)

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

  def getByID(idPaciente: Long): Future[Option[Paciente]] = {
    val pacienteByID = tabla.filter { f => f.id === idPaciente }.
      result.headOption
    db.run(pacienteByID)
  }

  def create(p: Paciente): Future[Paciente] = {
    val insercion = (tabla returning tabla.map(_.id)) += p
    db.run(insercion).map { nuevaID =>
      p.copy(id = nuevaID)
    }
  }

  def update(id: Long, p1: Paciente, d: DatosPaciente) = {
    val pacienteActualizado = p1.copy(
      nombre = d.nombre,
      apellido = d.apellido,
      dni = d.dni,
      obraSocial = d.obrasocial)
    val q = for {p <- tabla if p.id === id} yield p
    val updateAction = q.update(pacienteActualizado)
    db.run(updateAction)
  }

  def delete(idPaciente: Long) = {
    val q = tabla.filter(_.id === idPaciente)
    val deleteAction = q.delete
    db.run(deleteAction)
  }

  def fromDatos(d: DatosPaciente) = {
    val idNueva = UUID.randomUUID.getLeastSignificantBits
    Paciente(idNueva, d.nombre, d.apellido, d.dni, d.obrasocial)
  }

  def toDatos(p: Paciente) = {
    DatosPaciente(p.nombre, p.apellido, p.dni, p.obraSocial)
  }
}