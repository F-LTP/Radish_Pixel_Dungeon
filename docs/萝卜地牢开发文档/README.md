TheCatist（猫权主义者）重构了大量来自破碎像素地牢（Shattered Pixel Dungeon）原版的代码，以提升开发体验、使项目更加工程化。因此，本项目不少系统的行为已与原版产生差异。

人类开发者在参与本项目前，务必通读本文件夹内的系统文档，切勿依赖旧有经验，以免因认知错位引入错误。

AI Agent 首次介入时应通读本文件夹内文档，并将关键约定提炼写入 AGENTS.md；后续开发以 AGENTS.md 为准，无需每次重复阅读完整文档，但遇到疑似与旧经验不符的行为时，应回查对应系统文档确认。

## 目录内容

本文件夹收纳本项目当前**已实现系统**的设计/使用文档（每份文档标题下均标注实现状态）：

| 文档 | 系统 | 状态 |
|------|------|------|
| `damage-type-system-design.md` | 伤害类型系统 | ✅ 已实现（迁移进行中） |
| `damage-system-refactor-test-log.md` | 伤害系统重构日志/测试清单 | 📋 记录 |
| `event-system-usage.md` | 事件系统（设计+操作） | ✅ 已实现 |
| `branch_system.md` | 分支楼层系统 | ✅ 已实现 |
| `stair_pair_id_system.md` | 楼梯配对 ID 系统 | ✅ 已实现 |
| `runtime-atlas-resource-guide.md` | Runtime Atlas 图标资源 | ✅ 已实现 |
| `snd_items.md` | Slice&Dice 图标加载 | ✅ 已实现 |
| `DiceMageUIDesign.md` | UI 设计与布局 | ✅ 已实现 |
| `GLSL_Shader_System.md` | GLSL 着色器系统 | ✅ 已实现 |
| `hero-skin-system.md` | 英雄皮肤系统（持久化 + 独立文字键） | ✅ 已实现 |
| `jumble-skin-and-shift-system.md` | 杂散皮肤与变身机制 | ✅ 已实现 |
| `animation-no-time-block.md` | 不消耗时间播放动画的通用机制 | ✅ 已实现 |
| `changelog_docs.md` | Changelog 撰写指南 | 📖 指南 |

> 未实现的架构重构方案放在 `../萝卜地牢进度文档/重构计划/`，未实现的功能设想放在 `../萝卜地牢进度文档/待办/`。
