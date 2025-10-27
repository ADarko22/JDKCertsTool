package edu.adarko22.jdkcerts.output

class CommandPrinter(
    private val printer: PrinterFunction = ::println,
) {
    fun print(message: String) = printer(message)
}
