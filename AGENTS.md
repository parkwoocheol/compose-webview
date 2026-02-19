# AGENTS.md

## Purpose
This file is a thin entrypoint for all agents.
Detailed project guidance lives under `.agent/`.

## Instruction Precedence
1. System instructions
2. Developer instructions
3. User instructions
4. Repository instructions (`AGENTS.md`, `.agent/*`, project docs)
5. Agent defaults

## Canonical Sources
Use these files as the source of truth:
- Project overview: `.agent/knowledge/overview.md`
- Architecture: `.agent/knowledge/architecture.md`
- Tech stack/platform constraints: `.agent/knowledge/tech_stack.md`
- Commands/workflow: `.agent/knowledge/commands.md`
- Code style: `.agent/knowledge/code_style.md`
- Skills index: `.agent/skills/README.md`
- Skill dependencies: `.agent/skills/DEPENDENCIES.md`

## Skills Policy
- Use a skill when explicitly requested or when task type clearly matches a skill description.
- Read `SKILL.md` first, then only the minimum referenced files needed.
- Prefer repository scripts/templates/assets referenced by the skill.

## Knowledge Source
Use `.agent/knowledge/*` as the single source of project guidance.

## Agent Notes
- Codex reads `AGENTS.md` directly.
- `CLAUDE.md` and `GEMINI.md` should stay symlinked to `AGENTS.md`.
