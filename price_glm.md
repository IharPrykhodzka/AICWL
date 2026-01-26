# GLM API Pricing

> **Last Updated:** 2025-01-25
> **Source:** [https://open.bigmodel.cn/pricing](https://open.bigmodel.cn/pricing)
> **Currency:** USD per 1M tokens

---

## 📝 Text Models

| Model | Input | Cached Input | Cached Storage | Output | Context |
|-------|-------|--------------|----------------|--------|---------|
| ### 🔴 SENIOR TIER | | | | | |
| **GLM-4.7** | $0.60 | $0.11 | Limited-time Free | $2.20 | 128K |
| **GLM-4.6** | $0.60 | $0.11 | Limited-time Free | $2.20 | 128K |
| **GLM-4.5** | $0.60 | $0.11 | Limited-time Free | $2.20 | 128K |
| **GLM-4.5-X** | $2.20 | $0.45 | Limited-time Free | $8.90 | 128K |
| **GLM-4.5-AirX** | $1.10 | $0.22 | Limited-time Free | $4.50 | 128K |
| ### 🟡 MIDDLE TIER | | | | | |
| **GLM-4.6V** | $0.30 | $0.05 | Limited-time Free | $0.90 | 128K |
| **GLM-4.5V** | $0.60 | $0.11 | Limited-time Free | $1.80 | 128K |
| **GLM-4.6V-FlashX** | $0.04 | $0.004 | Limited-time Free | $0.40 | 128K |
| **GLM-4.7-FlashX** | $0.07 | $0.01 | Limited-time Free | $0.40 | 128K |
| ### 🟢 JUNIOR TIER | | | | | |
| **GLM-4.5-Air** | $0.20 | $0.03 | Limited-time Free | $1.10 | 98K |
| **GLM-4-32B** | $0.10 | - | - | $0.10 | 128K |
| ### 🆓 FREE MODELS | | | | | |
| **GLM-4.7-Flash** | **Free** | **Free** | **Free** | **Free** | 128K |
| **GLM-4.6V-Flash** | **Free** | **Free** | **Free** | **Free** | 128K |
| **GLM-4.5-Flash** | **Free** | **Free** | **Free** | **Free** | 128K |

---

## 🛠️ Built-in Tools

| Tool | Cost |
|------|------|
| **Web Search** | $0.01 / use |

---

## 🖼️ Image Generation

| Model | Price per Image |
|-------|-----------------|
| **GLM-Image** | $0.015 |
| **CogView-4** | $0.010 |

---

## 🎬 Video Generation

| Model | Price per Video |
|-------|-----------------|
| **CogVideoX-3** | $0.20 |
| **ViduQ1-Text** | $0.40 |
| **ViduQ1-Image** | $0.40 |
| **ViduQ1-Start-End** | $0.40 |
| **Vidu2-Image** | $0.20 |
| **Vidu2-Start-End** | $0.20 |
| **Vidu2-Reference** | $0.40 |

---

## 🎤 Audio Models

| Model | Price |
|-------|-------|
| **GLM-ASR-2512** | $0.03 / MTok (~$0.0024/minute) |

---

## 🤖 Agents

| Agent | Price |
|-------|-------|
| **GLM Slide/Poster Agent (beta)** | $0.70 / MTok |
| **General-Purpose Translation** | $3.00 / MTok |
| **Popular Special Effects Video Templates** | $0.20 / video |

---

## 📊 Cost Examples

### Example 1: Simple Chat (GLM-4.7-FlashX - Free)
- **Input:** 500 tokens (~375 words)
- **Output:** 300 tokens (~225 words)
- **Cost:** $0.00 (Free model)

### Example 2: Complex Task (GLM-4.7)
- **Input:** 2,000 tokens (~1,500 words)
- **Output:** 1,000 tokens (~750 words)
- **Cost:** (2,000 × $0.60 + 1,000 × $2.20) / 1M = **$0.0034**

### Example 3: Large Context (GLM-4.6V)
- **Input:** 50,000 tokens (~37,500 words, ~150 pages)
- **Output:** 5,000 tokens (~3,750 words)
- **Cost:** (50,000 × $0.30 + 5,000 × $0.90) / 1M = **$0.0195**

---

## 💡 Tips for Cost Optimization

1. **Use Flash models** for development and testing (GLM-4.7-Flash, GLM-4.6V-Flash, GLM-4.5-Flash)
2. **Use Junior tier** (GLM-4.5-Air, GLM-4-32B) for simple tasks
3. **Enable caching** for repeated prompts (saves ~80-90% on input costs)
4. **Use Middle tier** (GLM-4.6V, GLM-4.7-FlashX) for balanced performance
5. **Reserve Senior tier** (GLM-4.7, GLM-4.5-X) for complex tasks only

---

*Prices are subject to change. Always check the official [GLM pricing page](https://open.bigmodel.cn/pricing) for the most up-to-date information.*
