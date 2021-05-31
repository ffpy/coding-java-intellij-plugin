package org.ffpy.plugin.coding.util

import org.apache.velocity.VelocityContext
import org.apache.velocity.app.VelocityEngine
import org.apache.velocity.runtime.RuntimeConstants
import org.apache.velocity.runtime.log.NullLogChute
import java.io.StringWriter

object TemplateUtils {

    private val velocityEngine: VelocityEngine by lazy {
        val velocityEngine = VelocityEngine()
        // Disable separate Velocity logging.
        velocityEngine.setProperty(
            RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,
            NullLogChute::class.java.name
        )
        velocityEngine.init()
        velocityEngine
    }

    fun fromString(template: String, params: Map<String, Any>): String {
        val context = VelocityContext()
        params.forEach { (key: String?, value: Any?) -> context.put(key, value) }
        val writer = StringWriter()
        velocityEngine.evaluate(context, writer, "", template)
        return writer.toString()
    }
}