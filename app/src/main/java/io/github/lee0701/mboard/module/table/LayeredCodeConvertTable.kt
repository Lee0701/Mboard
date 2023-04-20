package io.github.lee0701.mboard.module.table

import io.github.lee0701.mboard.service.KeyboardState
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("layered")
class LayeredCodeConvertTable(
    @Serializable private val layers: Map<String, CodeConvertTable>,
): CodeConvertTable {

    fun get(layerId: String): CodeConvertTable? {
        return layers[layerId]
    }

    fun get(layerId: String, keyCode: Int, state: KeyboardState): Int? {
        return get(layerId)?.get(keyCode, state) ?: get(BASE_LAYER_NAME)?.get(keyCode, state)
    }

    fun getAllForState(layerId: String, state: KeyboardState): Map<Int, Int> {
        return layers[layerId]?.getAllForState(state)?: mapOf()
    }

    override fun get(keyCode: Int, state: KeyboardState): Int? {
        return get(BASE_LAYER_NAME)?.get(keyCode, state)
    }

    override fun getAllForState(state: KeyboardState): Map<Int, Int> {
        return getAllForState(BASE_LAYER_NAME, state)
    }

    companion object {
        const val BASE_LAYER_NAME = "base"
    }
}