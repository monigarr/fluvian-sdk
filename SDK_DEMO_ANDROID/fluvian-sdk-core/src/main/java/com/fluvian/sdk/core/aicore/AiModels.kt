// NOTE: This file contains a simplified implementation for evaluation.
// Advanced optimization logic is part of the private enterprise layer.

package com.fluvian.sdk.core.aicore

/**
 * File: AiModels.kt
 * Description: Reference JSON schema text for structured optimization decisions (documentation + host validation).
 *
 * **PRO:** hardened schemas, per-tenant enums, and policy metadata are distributed through private channels.
 *
 * Author: monigarr@monigarr.com
 * Date: 2026-04-18
 * Version: 1.3.6
 *
 * Usage:
 *   Attach to [AIConfig.structuredOutputJsonSchema] when prototyping enterprise gateways.
 *
 * Usage example:
 *   DEFAULT_OPTIMIZATION_JSON_SCHEMA
 */
val DEFAULT_OPTIMIZATION_JSON_SCHEMA: String =
    """
    {
      "type": "object",
      "required": ["decision"],
      "properties": {
        "decision": {
          "type": "string",
          "enum": ["INCREASE_QUALITY", "REDUCE_QUALITY", "STABILIZE"]
        }
      },
      "additionalProperties": true
    }
    """.trimIndent()
