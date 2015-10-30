package models

import play.api.Play._
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.{Json, Format}
import slick.driver.JdbcProfile
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.Future


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

  def create(medico: Medico): Future[Medico] = {
    val insercion = (tabla returning tabla.map(_.id)) += medico

    val insertedIDFuture = db.run(insercion)

    val copiaMedico: Future[Medico] = insertedIDFuture.map { nuevaID =>
      medico.copy(id = nuevaID)
    }

    copiaMedico
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

  def create(admin: Administrador): Future[Administrador] = {
    val insercion = (tabla returning tabla.map(_.id)) += admin

    val insertedIDFuture = db.run(insercion)

    val copiaAdmin: Future[Administrador] = insertedIDFuture.map { nuevaID =>
      admin.copy(id = nuevaID)
    }

    copiaAdmin
  }
}
