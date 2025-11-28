package edu.adarko22.jdkcerts.cli.output

/**
 * Default [ToolOutputPrinter] implementation that prints messages to a provided function
 * (by default, the standard output via [println]).
 */
class DefaultToolOutputPrinter(
    private val printer: PrintFn = ::println,
) : ToolOutputPrinter {
    override fun print(message: String) = printer(message)
}
