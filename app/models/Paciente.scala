package models

case class Paciente(
                     val nombre: String,
                     val apellido: String,
                     val dni: Long,
                     val obraSocial: Int
                     )

object Paciente {
  //companion object de Paciente
}