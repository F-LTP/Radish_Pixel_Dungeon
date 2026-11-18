# 萝卜地牢开发文档

本目录只收录当前实现的开发指南与有效开发规范。遇到文档与代码不一致时，以当前代码为准，并同步修正文档。

AI Agent 应先读项目根目录的 `AGENTS.md`，再按任务查阅本目录相关指南。历史方案、审查记录和未采用提案存放在 `../历史设计文档/`，不作为当前开发指引；只有用户明确要求了解历史决策或方案时才查阅。

## 当前指南

| 文档 | 范围 |
|------|------|
| `damage-type-system-design.md` | 伤害类型、修正阶段与伤害计算 |
| `event-system-usage.md` | 事件创建、订阅和发布 |
| `branch_system.md` | 分支楼层、楼梯配对与跨层切换 |
| `runtime-atlas-resource-guide.md` | Item、Talent、Buff 与英雄图标资源 |
| `snd_items.md` | Slice&Dice 图集区域加载 |
| `hero-skin-system.md` | 英雄皮肤定义与持久化 |
| `animation-no-time-block.md` | 不耗时动画阻塞机制 |
| `门系统与CombatRoom使用指南.md` | 特殊门、特殊楼梯与战斗房 |
| `enchantment-curse-sop.md` | 附魔与诅咒开发检查项 |
| `PD-生物开发原则.md` | 生物、NPC 与 Boss 的可复用开发原则 |
| `changelog_docs.md` | Changelog 撰写指南 |

未实现的架构重构方案和功能设想见 `../萝卜地牢进度文档/重构计划/` 与 `../萝卜地牢进度文档/待办/`。
