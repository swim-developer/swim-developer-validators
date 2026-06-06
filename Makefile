SONAR_URL   ?= http://localhost:9000
SONAR_TOKEN ?=

PARENT_DIR  := $(abspath $(dir $(abspath $(lastword $(MAKEFILE_LIST))))/..)
GITHUB_SSH  := git@github.com:swim-developer

SYNC_DEPS := swim-developer-root swim-developer-framework

.PHONY: help sync pull pull-deps install-deps install test sonar security-deps

help:
	@echo ""
	@echo "  swim-developer-validators — available targets"
	@echo "  ─────────────────────────────────────────────────────────"
	@echo ""
	@echo "    install          Build + install shared modules to local Maven repo"
	@echo "    test             Unit + integration tests for all shared modules"
	@echo "    sonar            SonarQube analysis (SONAR_TOKEN required)"
	@echo "    security-deps    OWASP Dependency-Check for shared modules"
	@echo ""
	@echo "  Shared modules:"
	@echo "    swim-validator-core      Base domain + ports"
	@echo "    swim-validator-consumer  Consumer-side shared logic"
	@echo "    swim-validator-provider  Provider-side shared logic"
	@echo ""
	@echo "  Validator-specific projects (each has its own Makefile):"
	@echo "    swim-dnotam-consumer-validator/"
	@echo "    swim-dnotam-provider-validator/"
	@echo "    swim-ed254-consumer-validator/"
	@echo "    swim-ed254-provider-validator/"
	@echo ""
	@echo "  Sync:"
	@echo "    sync               Full setup: pull + pull-deps + install-deps"
	@echo "    pull               Pull this project from remote"
	@echo "    pull-deps          Clone missing deps + pull existing ones in $(PARENT_DIR)"
	@echo "    install-deps       Install all deps into local Maven repository"

# ─── Sync ────────────────────────────────────────────────────────────────────

sync: pull pull-deps install-deps

pull:
	@echo ""
	@echo "  ── Pull this project ────────────────────────────────────────"
	@git pull --ff-only
	@echo ""

pull-deps:
	@echo ""
	@echo "  ── Ensure sibling dependencies in $(PARENT_DIR) ─────────────"
	@for repo in $(SYNC_DEPS); do \
	  dir="$(PARENT_DIR)/$$repo"; \
	  if [ ! -d "$$dir" ]; then \
	    echo "  CLONE   $$repo"; \
	    git clone "$(GITHUB_SSH)/$$repo.git" "$$dir" --quiet; \
	  else \
	    printf "  PULL    $$repo ... "; \
	    git -C "$$dir" pull --ff-only --quiet 2>&1 && echo "ok" || echo "skipped (local changes or detached HEAD)"; \
	  fi; \
	done
	@echo ""

install-deps:
	@echo ""
	@echo "  ── Install dependencies into local Maven repository ─────────"
	@for repo in $(SYNC_DEPS); do \
	  dir="$(PARENT_DIR)/$$repo"; \
	  if [ ! -d "$$dir" ]; then \
	    echo "  SKIP    $$repo (not found — run: make pull-deps)"; \
	    continue; \
	  fi; \
	  mvn_cmd="mvn"; \
	  [ -f "$$dir/mvnw" ] && mvn_cmd="$$dir/mvnw"; \
	  if [ "$$repo" = "swim-developer-root" ]; then \
	    args="install -N -DskipTests -q"; \
	  else \
	    args="clean install -DskipTests -q"; \
	  fi; \
	  printf "  INSTALL $$repo ... "; \
	  "$$mvn_cmd" -f "$$dir/pom.xml" $$args && echo "ok" || { echo "FAIL"; exit 1; }; \
	done
	@echo ""
	@echo "  Done. Run: make install"
	@echo ""

# ─── Build ───────────────────────────────────────────────────────────────────

install:
	./mvnw clean install -DskipTests

test:
	./mvnw verify -DskipITs=false

# ─── Quality ─────────────────────────────────────────────────────────────────

sonar:
	./mvnw clean verify sonar:sonar \
		-DskipITs=false \
		-Dsonar.host.url=$(SONAR_URL) \
		$(if $(SONAR_TOKEN),-Dsonar.login=$(SONAR_TOKEN),) \
		-Dsonar.projectKey=swim-developer-validators \
		-Dsonar.projectName=swim-developer-validators

security-deps:
	./mvnw org.owasp:dependency-check-maven:aggregate \
		-DfailBuildOnCVSS=7 -Dformats=HTML,JSON -DskipTests \
		-DsuppressionFile=owasp-suppressions.xml
	@echo "Report: target/dependency-check-report.html"
