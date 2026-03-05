# AGENTS.md

## Purpose
This file is a thin entrypoint for all agents.
Detailed project guidance lives under `.agents/`.

## Instruction Precedence
1. System instructions
2. Developer instructions
3. User instructions
4. Repository instructions (`AGENTS.md`, `.agents/*`, project docs)
5. Agent defaults

## Canonical Sources
Use these files as the source of truth:
- Project overview: `.agents/knowledge/overview.md`
- Architecture: `.agents/knowledge/architecture.md`
- Tech stack/platform constraints: `.agents/knowledge/tech_stack.md`
- Commands/workflow: `.agents/knowledge/commands.md`
- Code style: `.agents/knowledge/code_style.md`
- Skills index: `.agents/skills/README.md`
- Skill dependencies: `.agents/skills/DEPENDENCIES.md`

## Skills Policy
- Use a skill when explicitly requested or when task type clearly matches a skill description.
- Read `SKILL.md` first, then only the minimum referenced files needed.
- Prefer repository scripts/templates/assets referenced by the skill.

## Knowledge Source
Use `.agents/knowledge/*` as the single source of project guidance.

## Agent Notes
- Codex reads `AGENTS.md` directly.
- `CLAUDE.md` and `GEMINI.md` should stay symlinked to `AGENTS.md`.
