package io.github.andrefigas.rustjni.wasm

import io.github.andrefigas.rustjni.wasm.model.FunctionConfig
import io.github.andrefigas.rustjni.wasm.model.HostProvidedConfig
import io.github.andrefigas.rustjni.wasm.model.ReturnConfig
import org.gradle.api.Action

open class RustWasmExtension {
    var corePath: String = "./rust"
    var wasmHost: String = ""
    var mode: WasmMode = WasmMode.STANDALONE
    var wasmBridgePath: String = ""
    var chicoryVersion: String = "1.1.0"
    var webViewHost: String = ""
    var assetsPath: String = "www"
    var wasmFileName: String = "" // If empty, derived from bridge crate name
    var htmlFileName: String = "index.html" // Entry HTML file for browser mode WebView

    internal val functionConfigs = mutableMapOf<String, FunctionConfig>()

    fun functions(action: Action<FunctionsDsl>) {
        action.execute(FunctionsDsl(functionConfigs))
    }
}

class FunctionsDsl(private val configs: MutableMap<String, FunctionConfig>) {

    fun function(name: String, action: Action<FunctionConfigDsl>) {
        val dsl = FunctionConfigDsl()
        action.execute(dsl)
        configs[name] = FunctionConfig(name, dsl.hostProvidedParams.toList(), dsl.returnConfig)
    }
}

class FunctionConfigDsl {
    internal val hostProvidedParams = mutableListOf<HostProvidedConfig>()
    internal var returnConfig: ReturnConfig? = null

    fun hostProvided(paramName: String, provider: String = "") {
        hostProvidedParams.add(HostProvidedConfig(paramName, provider))
    }

    fun returns(action: Action<ReturnConfigDsl>) {
        val dsl = ReturnConfigDsl()
        action.execute(dsl)
        returnConfig = dsl.build()
    }
}

class ReturnConfigDsl {
    private var hostCallbackName: String? = null

    fun viaHostCallback(callbackName: String) {
        hostCallbackName = callbackName
    }

    internal fun build(): ReturnConfig? = hostCallbackName?.let { ReturnConfig(it) }
}
