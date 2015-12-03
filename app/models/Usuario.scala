package models

import java.util.UUID

import play.api.Play._
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.{Json, Format}
import slick.driver.JdbcProfile
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.Future

// fijarse de juntar medicos y administradores en la misma tabla .
sealed trait Usuario {
  val id: Long
  val nombre: String
  val apellido: String
  val usuario: String
  val password: String
  val rol: String
}

final case class Medico(
                         id: Long,
                         nombre: String,
                         apellido: String,
                         matricula: Long,
                         usuario: String,
                         password: String
                       )
  extends Usuario {
  val rol = "medico"
}

case class DatosMedico(nombre: String, apellido: String, matricula: Long, usuario: String, password: String)

object Medico {

  implicit val format: Format[Medico] = Json.format[Medico]
  protected val dbConfig = DatabaseConfigProvider.get[JdbcProfile](current)

  import dbConfig._
  import dbConfig.driver.api._

  class TablaMedicos(tag: Tag) extends Table[Medico](tag, "MEDICOS") {

    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)

    def nombre = column[String]("NOMBRE")

    def apellido = column[String]("APELLIDO")

    def matricula = column[Long]("MATRICULA")

    def usuario = column[String]("USUARIO")

    def password = column[String]("PASSWORD")

    def * = (id, nombre, apellido, matricula, usuario, password) <>((Medico.apply _).tupled, Medico.unapply _)
  }

  val tabla = TableQuery[TablaMedicos]

  def listar: Future[Seq[Medico]] = {
    val listaDeMedicos = tabla.result
    db.run(listaDeMedicos)
  }

  def getByID(idDoc: Long): Future[Option[Medico]] = {
    val docByID = tabla.filter { f => f.id === idDoc }.
      result.headOption

    db.run(docByID)
  }

  def getByUserAndPass(username: String, pass: String): Future[Option[Medico]] = {
    val docByUserAndPass = tabla.filter { f => f.usuario === username && f.password === pass }.
      result.headOption

    db.run(docByUserAndPass)
  }

  def create(medico: Medico): Future[Medico] = {
    val insercion = (tabla returning tabla.map(_.id)) += medico
    db.run(insercion).map { nuevaID =>
      medico.copy(id = nuevaID)
    }
  }

  def update(id: Long, m1: Medico, d: DatosMedico) = {
    val medicoActualizado = m1.copy(
      nombre = d.nombre,
      apellido = d.apellido,
      matricula = d.matricula,
      usuario = d.usuario,
      password = d.password)
    val q = for {m <- tabla if m.id === id} yield m
    val updateAction = q.update(medicoActualizado)
    db.run(updateAction)
  }

  def delete(idMedico: Long) = {
    val q = tabla.filter(_.id === idMedico)
    val deleteAction = q.delete
    db.run(deleteAction)
  }

  def fromDatos(d: DatosMedico) = {
    val idNueva = UUID.randomUUID.getLeastSignificantBits
    Medico(idNueva, d.nombre, d.apellido, d.matricula, d.usuario, d.password)
  }

  def toDatos(m: Medico) = {
    DatosMedico(m.nombre, m.apellido, m.matricula, m.usuario, m.password)
  }
}

final case class Administrador(
                                id: Long,
                                nombre: String,
                                apellido: String,
                                usuario: String,
                                password: String
                              )
  extends Usuario {
  val rol = "administrador"
}

case class DatosAdmin(nombre: String, apellido: String, usuario: String, password: String)

object Administrador {

  implicit val format: Format[Administrador] = Json.format[Administrador]
  protected val dbConfig = DatabaseConfigProvider.get[JdbcProfile](current)

  import dbConfig._
  import dbConfig.driver.api._

  class TablaAdministradores(tag: Tag) extends Table[Administrador](tag, "ADMINISTRADORES") {

    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)

    def nombre = column[String]("NOMBRE")

    def apellido = column[String]("APELLIDO")

    def usuario = column[String]("USUARIO")

    def password = column[String]("PASSWORD")

    def * = (id, nombre, apellido, usuario, password) <>((Administrador.apply _).tupled, Administrador.unapply _)
  }

  val tabla = TableQuery[TablaAdministradores]

  def listar: Future[Seq[Administrador]] = {
    val listaDeAdministradores = tabla.result
    db.run(listaDeAdministradores)
  }

  def getByID(idAdmin: Long): Future[Option[Administrador]] = {
    val adminByID = tabla.filter { f => f.id === idAdmin }.
      result.headOption

    db.run(adminByID)
  }

  def getByUserAndPass(username: String, pass: String): Future[Option[Administrador]] = {
    val docByUserAndPass = tabla.filter { f => f.usuario === username && f.password === pass }.
      result.headOption

    db.run(docByUserAndPass)
  }

  def create(admin: Administrador): Future[Administrador] = {
    val insercion = (tabla returning tabla.map(_.id)) += admin
    db.run(insercion).map { nuevaID =>
      admin.copy(id = nuevaID)
    }
  }

  def update(id: Long, admin: Administrador, datos: DatosAdmin) = {
    val adminActualizado = admin.copy(
      nombre = datos.nombre,
      apellido = datos.apellido,
      usuario = datos.usuario,
      password = datos.password)
    val q = for {a <- tabla if a.id === id} yield a
    val updateAction = q.update(adminActualizado)
    db.run(updateAction)
  }

  def delete(idAdmin: Long) = {
    val q = tabla.filter(_.id === idAdmin)
    val deleteAction = q.delete
    db.run(deleteAction)
  }

  def fromDatos(d: DatosAdmin) = {
    val idNueva = UUID.randomUUID.getLeastSignificantBits
    Administrador(idNueva, d.nombre, d.apellido, d.usuario, d.password)
  }

  def toDatos(a: Administrador) = {
    DatosAdmin(a.nombre, a.apellido, a.usuario, a.password)
  }

}
