<!-- component.md -->
---
description: 层次三 — 实现组件结构
allowed-tools: Bash, Write
---

## 目标

根据传入的 `$ARGUMENTS` 以及已通过 Layer 2 的事件逻辑，在 `$COMPONENTS_DIR` 下生成对应的纯函数组件实现。

## 执行步骤

1. 运行：
   ```bash
   echo "component" > .claude/.current-layer
   set -a
   source .claude/project.env
   set +a
   ```

2. 确认前置条件：
   - 使用 `.claude/project.env` 中的 `EVENTS_DIR`、`DOMAIN_FILE`、`APPSTATE_FILE`
   - 读取 `$DOMAIN_FILE` 和 `$APPSTATE_FILE`，理解组件所需的数据形状
   - 读取 `$EVENTS_DIR` 下相关业务文件，确认可以触发的事件类型

3. 分析需求：
   - 读取并理解 `$ARGUMENTS`
   - 如果 `$ARGUMENTS` 中包含 ASCII layout 草图，优先以草图为准确定空间结构
   - 明确每个组件的 props 来源、子组件层级，以及需要触发的事件

4. 生成组件实现：
   - 在 `$COMPONENTS_DIR` 下创建或更新对应业务文件
   - 每个组件实现为独立的纯函数，接收 props map，返回 cljfx 描述 map
   - 禁止在组件函数内直接修改 `*state`（无 `swap!`、无 `reset!`）
   - 禁止在此层 `require` 任何 `events/` 命名空间之外的副作用来源
   - 事件触发只能通过返回 map 中的 `:on-*` 键携带事件 map 实现

5. 不要主动执行验证：
   - 验证会由 hook 自动触发
   - 你只需要完成组件生成

6. 等待验证结果：
   - 必须等 hook 显式退出后再继续下一步
   - 如果验证失败，根据输出修正组件实现
   - 如果验证通过，再进入后续流程

## 约束

- 这一层只负责组件结构实现，不负责集成接线。
- 所有组件函数必须是纯函数，相同 props 输入必须返回相同描述 map。
- 如果验证输出有错误，优先按错误信息修正实现，禁止修改验证器逻辑。
