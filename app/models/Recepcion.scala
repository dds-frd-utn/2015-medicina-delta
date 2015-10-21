package models

import java.sql.Date

import play.api.Play._
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.{Json, Format}
import slick.driver.JdbcProfile
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.Future

case class Recepcion(
                      id: Long,
                      idPaciente: Long,
                      idMedico: Long,
                      fecha: Date,
                      estado: Int, // hasta implementar el state
                      diagnostico: String,
                      prioridad: String
                      )

object Recepcion {
  implicit val format: Format[Recepcion] = Json.format[Recepcion]
  protected val dbConfig = DatabaseConfigProvider.get[JdbcProfile](current)

  import dbConfig._
  import dbConfig.driver.api._

  class TablaRecepciones(tag: Tag) extends Table[Recepcion](tag, "RECEPCIONES") {

    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)

    def idPaciente = column[Long]("ID")

    def idMedico = column[Long]("ID")

    def fecha = column[Date]("FECHA")

    def estado = column[Int]("ESTADO")

    def diagnostico = column[String]("DIAGNOSTICO")

    def prioridad = column[String]("PRIORIDAD")

    def paciente = foreignKey("TB_PACIENTE", idPaciente, Paciente.tabla)(_.id)

    def medico = foreignKey("TB_MEDICO", idMedico, Medico.tabla)(_.id)

    def * = (id, idPaciente, idMedico, fecha, estado, diagnostico, prioridad) <>((Recepcion.apply _).tupled, Recepcion.unapply _)
  }

  val tabla = TableQuery[TablaRecepciones]

  def listar: Future[Seq[Recepcion]] = {
    val listaDeRecepciones = tabla.result
    db.run(listaDeRecepciones)
  }

  def getByID(idRecepcion: Long): Future[Option[Recepcion]] = {
    val recepcionByID = tabla.filter { f => f.id === idRecepcion }.
      result.headOption

    db.run(recepcionByID)
  }

  def getByPaciente(idPaciente: Long): Future[Seq[Recepcion]] = {
    val recepcionesByPaciente = tabla.filter { r => r.idPaciente === idPaciente }.
      result

    db.run(recepcionesByPaciente)
  }

  def getByMedico(idMedico: Long): Future[Seq[Recepcion]] = {
    val recepcionesByMedico = tabla.filter { r => r.idMedico === idMedico }.
      result

    db.run(recepcionesByMedico)
  }

  def create(recepcion: Recepcion): Future[Recepcion] = {
    val insercion = (tabla returning tabla.map(_.id)) += recepcion

    val insertedIDFuture = db.run(insercion)

    val copiaRecepcion: Future[Recepcion] = insertedIDFuture.map { nuevaID =>
      recepcion.copy(id = nuevaID)
    }

    copiaRecepcion
  }

}