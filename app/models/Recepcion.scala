package models

import java.util.Date

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
  // companion object de Recepcion
}