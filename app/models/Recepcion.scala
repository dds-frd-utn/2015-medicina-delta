package models


import java.sql.{Timestamp, Date}

import akka.actor.ActorRef
import play.api.Play._
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.{Json, Format}
import slick.driver.H2Driver._
import slick.driver.JdbcProfile
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.Future
import java.time._

/*
 TODO
 Cada vez que se crea una recepcion crear un actor con la misma id cuyo supervisor es el actor del websocket
 cada vez que se envia un mensaje al websocket, este lo dirige al actor correspondiente para cambiar su estado
 luego cuando ya es atendido lo destruye
 */
case class Recepcion(
                      id: Long,
                      idPaciente: Long,
                      idMedico: Long,
                      fecha: LocalDateTime,
                      diagnostico: Option[String],
                      prioridad: String
                    )

case class DatosRecepcion(
                           idPaciente: Long,
                           idMedico: Long,
                           diagnostico: Option[String],
                           prioridad: String
                         )

object Recepcion {
  implicit val format: Format[Recepcion] = Json.format[Recepcion]
  protected val dbConfig = DatabaseConfigProvider.get[JdbcProfile](current)

  import dbConfig._
  import dbConfig.driver.api._

  class TablaRecepciones(tag: Tag) extends Table[Recepcion](tag, "RECEPCIONES") {


    implicit val localDateTimeColumn = MappedColumnType.base[LocalDateTime, Timestamp](
      d => Timestamp.from(d.toInstant(ZoneOffset.ofHours(0))),
      d => d.toLocalDateTime
    )

    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)

    def idPaciente = column[Long]("IDPACIENTE")

    def idMedico = column[Long]("IDMEDICO")

    def fecha = column[LocalDateTime]("FECHA")

    def diagnostico = column[String]("DIAGNOSTICO")

    def prioridad = column[String]("PRIORIDAD")

    def paciente = foreignKey("TB_PACIENTE", idPaciente, Paciente.tabla)(_.id)

    def medico = foreignKey("TB_MEDICO", idMedico, Medico.tabla)(_.id)

    def * = (id, idPaciente, idMedico, fecha, diagnostico.?, prioridad) <>((Recepcion.apply _).tupled, Recepcion.unapply _)
  }

  val tabla = TableQuery[TablaRecepciones]

  def listar: Future[Seq[Recepcion]] = {
    val listaDeRecepciones = tabla.result
    db.run(listaDeRecepciones)
  }

  // dada una recepcion devuelve una 3-upla (recepcion,paciente,medico)
  def recepcionToTriple(r: Recepcion): Future[(Recepcion, Paciente, Medico)] = {
    for {
      p <- Paciente.getByID(r.idPaciente)
      m <- Medico.getByID(r.idMedico)
    } yield (r, p.get, m.get)
  }


  def getByID(idRecepcion: Long): Future[Option[Recepcion]] = {
    val recepcionByID = tabla.filter {
      f => f.id === idRecepcion
    }.result.headOption

    db.run(recepcionByID)
  }

  def getByPaciente(idPaciente: Long): Future[Seq[Recepcion]] = {
    val recepcionesByPaciente = tabla.filter {
      r => r.idPaciente === idPaciente
    }.result

    db.run(recepcionesByPaciente)
  }

  def getByMedico(idMedico: Long): Future[Seq[Recepcion]] = {
    val recepcionesByMedico = tabla.filter {
      r => r.idMedico === idMedico
    }.result

    db.run(recepcionesByMedico)
  }

  def create(recepcion: Recepcion): Future[Recepcion] = {
    val insercion = (tabla returning tabla.map(_.id)) += recepcion

    val insertedIDFuture = db.run(insercion)

    val copiaRecepcion: Future[Recepcion] = insertedIDFuture.map {
      nuevaID =>
        recepcion.copy(id = nuevaID)
    }

    copiaRecepcion
  }

}
