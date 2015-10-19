package models

import java.util.Date

case class Recepcion(
                      val idPaciente: Long,
                      val idMedico: Long,
                      val fecha: Date,
                      val estado: Int, // hasta implementar el state
                      val diagnostico: String,
                      val prioridad: String
                      )

object Recepcion {
  // companion object de Recepcion
}