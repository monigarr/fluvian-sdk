/**
 * File: LlmDecisionParser.kt
 * Description: Deterministic parsing helpers for model text / JSON fragments — used in tests and host-side tooling.
 *
 * NOTE:
 * This file contains a simplified implementation for evaluation purposes.
 * Advanced optimization logic is part of the private enterprise layer.
 *
 * Author: monigarr@monigarr.com
 * Date: 2026-04-18
 * Version: 1.3.6
 *
 * Usage:
 *   Prefer [parseOptimizationDecisionFromModelContent] for structured payloads; fall back to [parseLlmDecisionText] for tokens.
 *
 * Usage example:
 *   LlmDecisionParser.parseLlmDecisionText("REDUCE_QUALITY")
 */
package com.fluvian.sdk.core.aicore

import org.json.JSONObject

/**
 * Parses coarse optimization tokens from LLM outputs. Open Core stays regex/JSON-light; enterprise layers add schema
 * validation, safety filters, and red-team classifiers.
 */
object LlmDecisionParser {
    fun parseLlmDecisionText(text: String): OptimizationDecision = scanForDecisionToken(text)

    /**
     * Accepts JSON (including nested objects) or plain text; unknown content maps to [OptimizationDecision.STABILIZE]
     * only when no token matches.
     */
    fun parseOptimizationDecisionFromModelContent(content: String): OptimizationDecision {
        val trimmed = content.trim()
        val fromJson =
            runCatching { extractFromJsonRoot(JSONObject(trimmed)) }.getOrNull()
        if (fromJson != null) return fromJson
        return scanForDecisionToken(trimmed)
    }

    private fun extractFromJsonRoot(root: JSONObject): OptimizationDecision? {
        root.optString("decision", "").takeIf { it.isNotBlank() }?.let { return tokenToDecision(it) }
        if (root.has("payload") && root.get("payload") is JSONObject) {
            val payload = root.getJSONObject("payload")
            payload.optString("result", "").takeIf { it.isNotBlank() }?.let { return tokenToDecision(it) }
        }
        return null
    }

    private fun scanForDecisionToken(text: String): OptimizationDecision {
        val upper = text.uppercase()
        return when {
            "REDUCE_QUALITY" in upper -> OptimizationDecision.REDUCE_QUALITY
            "INCREASE_QUALITY" in upper -> OptimizationDecision.INCREASE_QUALITY
            "STABILIZE" in upper -> OptimizationDecision.STABILIZE
            else -> OptimizationDecision.STABILIZE
        }
    }

    private fun tokenToDecision(raw: String): OptimizationDecision = scanForDecisionToken(raw)
}
