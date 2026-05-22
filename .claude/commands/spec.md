<!-- spec.md -->
---
description: 层次一 — 形式化 malli schema
allowed-tools: Bash, Write
---

## 目标

根据传入的 `$ARGUMENTS` 分析需求，并在 `$SPEC_DIR` 下生成对应的 malli schema 定义。

必须保证项目存在可供事件逻辑验证使用的根 schema：`AppState`。

## 执行步骤

1. 运行：
   ```bash
   echo "spec" > .claude/.current-layer
   set -a
   source .claude/project.env
   set +a
   ```
   
2. 读取项目配置：
   - 使用 `.claude/project.env` 中的 `SPEC_DIR`、`APPSTATE_FILE`
   - 如果任一变量缺失，先停止并报告缺失项

3. 分析需求：
   - 读取并理解 `$ARGUMENTS`
   - 明确要建模的对象、字段、约束和结构
   - 判断这些变更是否影响应用状态结构

4. 生成 schema：
   - 在 `$SPEC_DIR` 下创建或更新 malli schema 文件
   - 保持结构清晰、命名一致、便于后续扩展
   - 优先复用已有 schema，避免重复定义

5. 维护根状态 schema：
   - 必须保证 `$APPSTATE_FILE` 存在
   - 必须保证其中定义并导出 `AppState`
   - 如果本次需求涉及应用状态字段、事件读写字段、页面状态、表单状态或领域状态变更，必须同步更新 `AppState`
   - 不允许只新增局部 schema 而遗漏 `AppState` 对这些字段的整合

6. 不要主动执行验证：
   - 验证会由 hook 自动触发
   - 你只需要完成 schema 生成

7. 等待验证结果：
   - 必须等 hook 显式退出后再继续下一步
   - 如果验证失败，根据输出修正 schema
   - 如果验证失败与 `AppState` 缺失或不匹配有关，优先修复 `$APPSTATE_FILE`
   - 如果验证通过，再进入后续流程

## 约束

- 这一层只负责 schema 生成，不负责手动校验。
- `AppState` 是事件逻辑验证的基础契约，必须始终存在且可引用。
- 新增或修改状态相关 schema 时，必须检查是否需要同步更新 `AppState`。
- 如果需求尚不足以确定完整状态结构，也必须保留一个合法、最小可用的 `AppState`。
- 如果验证输出有错误，优先按错误信息修正 schema。
