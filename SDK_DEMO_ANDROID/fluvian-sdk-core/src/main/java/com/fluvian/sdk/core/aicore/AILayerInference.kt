// NOTE: This file contains a simplified implementation for evaluation.
// Advanced optimization logic is part of the private enterprise layer.

package com.fluvian.sdk.core.aicore

import android.content.Context
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

/**
 * File: AILayerInference.kt
 * Description: Coroutine-first inference façade over [AIManager] — Open Core stays deterministic and side-effect free off-thread.
 *
 * **PRO:** async model pipelines, batching, and safety classifiers extend this class in commercial artifacts.
 * Advanced AI logic is part of the enterprise layer.
 *
 * Author: monigarr@monigarr.com
 * Date: 2026-04-18
 * Version: 1.3.6
 *
 * Usage:
 *   Call [configure] once, then [infer] from a [kotlinx.coroutines.CoroutineScope] on [Dispatchers.Default].
 *
 * Usage example:
 *   val layer = AILayerInference()
 */
class AILayerInference
    @JvmOverloads
    constructor(
        private val aiManager: AIManager = AIManager(),
        private val coroutineContext: CoroutineContext = Dispatchers.Default,
    ) {
        constructor(
            aiManager: AIManager,
            dispatcher: CoroutineDispatcher,
        ) : this(aiManager, dispatcher as CoroutineContext)

        fun configure(
            config: AIConfig,
            applicationContext: Context? = null,
            providerResolver: AIProviderResolver? = null,
        ) {
            aiManager.configure(config, applicationContext?.applicationContext, providerResolver)
        }

        suspend fun infer(qos: QoSData): OptimizationDecision =
            withContext(coroutineContext) {
                aiManager.predict(qos)
            }

        fun inferFlow(qosFlow: Flow<QoSData>): Flow<OptimizationDecision> =
            qosFlow.map { snapshot ->
                aiManager.predict(snapshot)
            }

        fun shutdown() {
            aiManager.shutdown()
        }
    }
