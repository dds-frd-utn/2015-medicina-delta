package models

case class Paciente(
                     id: Long,
                     nombre: String,
                     apellido: String,
                     dni: Long,
                     obraSocial: Int
                     )

object Paciente {
  //companion object de Paciente
}