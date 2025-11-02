package edu.adarko22.jdkcerts.cli.output

class DefaultToolOutputPrinter(
    private val printer: PrinterFunction = ::println,
) : ToolOutputPrinter {
    override fun print(message: String) = printer(message)
}
