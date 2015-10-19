package models


sealed trait Usuario {
  val id: Long
  val nombre: String
  val apellido: String
  val usuario: String
  val password: String
  val rol: String
}

final case class Medico(id: Long,
                        nombre: String,
                        apellido: String,
                        matricula: Long,
                        usuario: String,
                        password: String,
                        rol: String = "medico")
  extends Usuario

object Medico {
  // companion object de clase medico
}

final case class Administrador(id: Long,
                               nombre: String,
                               apellido: String,
                               usuario: String,
                               password: String,
                               rol: String = "administrador")
  extends Usuario

object Administrador {
  // companion object de clase administrador
}
