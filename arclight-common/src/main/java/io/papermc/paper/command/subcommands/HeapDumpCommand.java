package io.papermc.paper.command.subcommands;

import io.papermc.paper.command.PaperSubcommand;
import org.bukkit.command.CommandSender;

import javax.management.MBeanServer;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class HeapDumpCommand implements PaperSubcommand {
    @Override
    public boolean execute(final CommandSender sender, final String subCommand, final String[] args) {
        try {
            MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            File heapDump = new File(new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date()) + "-heap.hprof");

            server.invoke(
                    ManagementFactory.newPlatformMXBeanProxy(server, "com.sun.management:type=HotSpotDiagnostic", com.sun.management.HotSpotDiagnosticMXBean.class).getObjectName(),
                    "dumpHeap",
                    new Object[]{heapDump.getAbsolutePath(), true},
                    new String[]{"java.lang.String", "boolean"}
            );

            sender.sendMessage("Heap dump saved to " + heapDump.getAbsolutePath());
        } catch (Exception e) {
            sender.sendMessage("Failed to create heap dump: " + e.getMessage());
        }
        return true;
    }
}