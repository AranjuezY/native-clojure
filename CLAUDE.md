## 项目结构
src/
└── myapp/
    │
    ├── specs/
    │   ├── domain.clj
    │   ├── events.clj
    │   └── state.clj       ← AppState
    │
    ├── events/
    │   ├── dispatch.clj
    │   ├── middleware.clj  ← (optional)
    │   └── <任意业务名>.clj
    │
    ├── ui/
    │   ├── root.clj
    │   └── <任意业务名>.clj
    │
    └── core.clj

## 项目约定
- 所有状态变更必须通过 handle-event 多方法
- 新功能必须先定义 malli spec，再实现逻辑
- 组件必须是纯函数，不得有副作用
- 禁止在 events/ 中 require components/ 命名空间
- 禁止在 ui/ 中直接修改 *state

## 三层执行顺序（每次任务必须遵守）
1. Spec 形式化  → Layer 1 通过才继续
2. 事件逻辑     → Layer 2 通过才继续
3. 组件结构     → Layer 3 通过才继续

## 验证失败规则
- 任意层失败：停止当前层，修正后重新验证
- 禁止跳层、禁止修改验证器逻辑使测试通过

## 构建约定
- dev/ 仅用于开发，不进入生产构建
- deps.edn :dev alias 包含 src/dev，:prod alias 不包含
