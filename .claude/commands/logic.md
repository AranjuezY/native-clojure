<!-- logic.md -->
---
description: 层次二 — 实现事件逻辑
allowed-tools: Bash, Write
---

## 目标

根据传入的 `$ARGUMENTS` 以及已通过 Layer 1 的 malli schema，在 `$EVENTS_DIR` 下生成对应的 `handle-event` defmethod 实现。

## 执行步骤

1. 运行：
   ```bash
   echo "logic" > .claude/.current-layer
   set -a
   source .claude/project.env
   set +a
   ```

2. 确认前置条件：
   - 使用 `.claude/project.env` 中的 `EVENTS_DIR`、`SPEC_DIR`
   - 读取 `$SPEC_DIR` 下相关 schema 文件，理解状态结构与事件 payload 的数据契约

3. 分析需求：
   - 读取并理解 `$ARGUMENTS`
   - 明确每个事件的触发条件、状态转换路径、以及对 AppState 的影响范围

4. 生成事件逻辑：
   - 在 `$EVENTS_DIR` 下创建或更新对应业务文件
   - 每个事件实现为独立的 `defmethod handle-event` 
   - 所有实现必须是纯函数：接收 event map 和当前 state，返回新 state
   - 禁止在此层 `require` 任何 `components/` 命名空间
   - 禁止产生副作用（无 `swap!`、无 IO）

5. 不要主动执行验证：
   - 验证会由 hook 自动触发
   - 你只需要完成事件逻辑生成

6. 等待验证结果：
   - 必须等 hook 显式退出后再继续下一步
   - 如果验证失败，根据输出修正 defmethod 实现
   - 如果验证通过，再进入后续流程

## 约束

- 这一层只负责事件逻辑实现，不负责组件结构。
- 所有 defmethod 的输入输出必须满足已定义的 malli schema。
- 如果验证输出有错误，优先按错误信息修正实现，禁止修改验证器逻辑。
