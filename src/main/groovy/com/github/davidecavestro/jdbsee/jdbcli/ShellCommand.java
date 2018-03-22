package com.github.davidecavestro.jdbsee.jdbcli;

import jdk.nashorn.internal.ir.Terminal;
import org.jline.reader.*;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AttributedString;
import picocli.CommandLine;

import javax.inject.Inject;
import java.io.IOException;

@CommandLine.Command(name = "shell", description = "Start the command shell")
public class ShellCommand implements Runnable {
  @CommandLine.ParentCommand
  private MainCommand parent;

  @Inject//dagger
  ShellCommand(){}

  @Override
  public void run () {
    try {
      TerminalBuilder builder = TerminalBuilder.builder();

      Terminal terminal = builder.build();

      LineReader reader = LineReaderBuilder.builder()
          .terminal(terminal)
          .completer(completer)
          .parser(parser)
          .build();
      while (true) {
        String line = null;
        try {
          line = reader.readLine(prompt, rightPrompt, (MaskingCallback) null, null);
        } catch (UserInterruptException e) {
          // Ignore
        } catch (EndOfFileException e) {
          return;
        }
        if (line == null) {
          continue;
        }

        line = line.trim();

        if (color) {
          terminal.writer().println(
              AttributedString.fromAnsi("\u001B[33m======>\u001B[0m\"" + line + "\"")
                  .toAnsi(terminal));

        } else {
          terminal.writer().println("======>\"" + line + "\"");
        }
        terminal.flush();

        // If we input the special word then we will mask
        // the next line.
        if ((trigger != null) && (line.compareTo(trigger) == 0)) {
          line = reader.readLine("password> ", mask);
        }
        if (line.equalsIgnoreCase("quit") || line.equalsIgnoreCase("exit")) {
          break;
        }
        ParsedLine pl = reader.getParser().parse(line, 0);
        if ("set".equals(pl.word())) {
          if (pl.words().size() == 3) {
            reader.setVariable(pl.words().get(1), pl.words().get(2));
          }
        }
      }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      //    CommandLine commandLine = new CommandLine (parent, daggerFactory);
//    for (final String[] args : multiArgs) {
//      commandLine.parseWithHandler(new CommandLine.RunLast(), System.err, args);
//    }
  }
}
