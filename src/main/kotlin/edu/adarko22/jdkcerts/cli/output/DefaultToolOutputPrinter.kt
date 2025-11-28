package edu.adarko22.jdkcerts.cli.output

class DefaultToolOutputPrinter(
    private val printer: PrintFn = ::println,
) : ToolOutputPrinter {
    override fun print(message: String) = printer(message)
}
