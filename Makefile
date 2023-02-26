DATADIR_FOR_LAUNCHER := /usr/share/games/heart
DATADIR     := $(DESTDIR)$(DATADIR_FOR_LAUNCHER)
DESKTOPDIR  := $(DESTDIR)/usr/share/applications/
DESKTOPFILE := $(DESKTOPDIR)/heart.desktop
LAUNCHERDIR := $(DESTDIR)/usr/games
LAUNCHER    := $(LAUNCHERDIR)/heart
MANDIR      := $(DESTDIR)/usr/share/man/man6
MANPAGE     := $(MANDIR)/heart.6

.PHONY: run clean uninstall

bin/Game.class: Game.java
	if [ -d bin ]; then rm -r bin; fi
	mkdir bin
	javac -d bin Game.java

bin/Heart.jar: bin/Game.class
	jar cfe bin/Heart.jar Game -C bin . -C images .
	chmod +x bin/Heart.jar

run: bin/Game.class
	java -cp bin:images Game

clean:
	! [ -d bin ] || rm -r bin

uninstall:
	! [ -d $(DATADIR)     ] || rm -r $(DATADIR)
	! [ -e $(LAUNCHER)    ] || rm    $(LAUNCHER)
	! [ -e $(DESKTOPFILE) ] || rm    $(DESKTOPFILE)
	! [ -e $(MANPAGE)     ] || rm    $(MANPAGE)

install: bin/Heart.jar uninstall
	[ -d $(DATADIR)     ] || mkdir -p $(DATADIR)
	[ -d $(LAUNCHERDIR) ] || mkdir -p $(LAUNCHERDIR)
	[ -d $(DESKTOPDIR)  ] || mkdir -p $(DESKTOPDIR)
	[ -d $(MANDIR)      ] || mkdir -p $(MANDIR)
	cp bin/Heart.jar $(DATADIR)/Heart.jar
	@echo "#!/bin/sh"                                   >> $(LAUNCHER)
	@echo "java -jar $(DATADIR_FOR_LAUNCHER)/Heart.jar" >> $(LAUNCHER)
	chmod +x $(LAUNCHER)
	cp heart.desktop $(DESKTOPFILE)
	cp manpage $(MANPAGE)
