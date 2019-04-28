package com.andreacioccarelli.androoster.core;

import android.os.Build;

import com.andreacioccarelli.androoster.tools.PreferencesBuilder;
import com.jrummyapps.android.shell.Shell;

import static com.andreacioccarelli.androoster.core.TerminalCore.*;

/**
 * Created by andrea on 2017/nov.
 * Part of the package com.andreacioccarelli.androoster.core
 *
 * Every method has to be be executed in a thread.
 * Live template KEY ncore
 */

@SuppressWarnings({
        "SameParameterValue",
        "WeakerAccess"
})
public class Core extends CoreBase {

    /**
     * Boosts GPU
     * */
    public static void gpu_boost(boolean s) {
        new Thread(() -> {
        if (s) {
            Companion.setprop("hw3d.force", 1);
            Companion.buildprop("ro.product.gpu.driver", 0);
            Companion.buildprop("persist.sampling_profiler", 0);
            Companion.buildprop("hwui.render_dirty_regions", false);
            Companion.buildprop("persist.sampling_profiler", 0);
            Companion.buildprop("persist.sys.ui.hw", 1);
            Companion.buildprop("ro.config.disable.hw_accel", false);
            Companion.buildprop("video.accelerate.hw", 1);
            Companion.buildprop("debug.egl.profiler", 1);
            Companion.buildprop("render.sw.hw", 1);
            Companion.buildprop("ro.vold.umsdirtyratio", 20);
            Companion.buildprop("hwui.disable_vsync", true);
        } else {
            Companion.buildprop("debug.egl.profiler", 0);
            Companion.setprop("hw3d.force", 0);
            Companion.buildprop("debug.egl.hw", 0);
            Companion.buildprop("render.sw.hw", 0);
            INSTANCE.run("rm -f /data/property/persist.sampling_profiler\n" +
                    "rm -f /data/property/persist.sys.ui.hw\n" +
                    "rm -f /data/property/persist.sys.composition.type");
        }
        }).start();
    }

    /**
     * Enables default android library for media management
     * */
    public static void set_stagefright(boolean s) {
        new Thread(() -> {
        if (s) {
            Companion.buildprop("media.stagefright.enable-player", true);
            Companion.buildprop("media.stagefright.enable-meta", true);
            Companion.buildprop("media.stagefright.enable-on", true);
            Companion.buildprop("media.stagefright.enable-http", true);
            Companion.buildprop("media.stagefright.enable-rtsp", true);
            Companion.buildprop("media.stagefright.enable-aac", true);
            Companion.buildprop("media.stagefright.enable-qcp", true);
            Companion.buildprop("media.stagefright.enable-record", false);
        } else {
            Companion.buildprop("media.stagefright.enable-player", false);
            Companion.buildprop("media.stagefright.enable-meta", false);
            Companion.buildprop("media.stagefright.enable-on", false);
            Companion.buildprop("media.stagefright.enable-http", false);
            Companion.buildprop("media.stagefright.enable-rtsp", false);
            Companion.buildprop("media.stagefright.enable-aac", false);
            Companion.buildprop("media.stagefright.enable-qcp", false);
            Companion.buildprop("media.stagefright.enable-record", false);
        }
        }).start();
    }

    /**
     * Turns on hardware acceleration
     */
    public static void hardware_acceleration(boolean s) {
        new Thread(() -> {
        if (s) {
            Companion.buildprop("video.accelerate.hw", 1);
        } else {
            Companion.buildprop("video.accelerate.hw", 0);
        }
        }).start();
    }

    /**
     * Enables UI to be rendered via gpu only
     */
    public static void drawing_with_gpu(boolean s) {
        new Thread(() -> {
        if (s) {
            Companion.buildprop("persist.sys.composition.type", FrameworkSurface.Companion.cpu_composition_method);
            Companion.buildprop("debug.composition.type", FrameworkSurface.Companion.cpu_composition_method);
        } else {
            Companion.buildprop("persist.sys.composition.type", FrameworkSurface.Companion.cpu_composition_method);
            Companion.buildprop("debug.composition.type", FrameworkSurface.Companion.cpu_composition_method);
        }
        }).start();
    }

    /**
     * Tweaks JPEG quality
     */
    public static void tweak_jpeg(boolean s) {
        new Thread(() -> {
        if (s) {
            Companion.buildprop("ro.media.dec.jpeg.memcap", "8000000");
            Companion.buildprop("ro.media.enc.hprof.vid.bps", "8000000");
            Companion.buildprop("ro.media.enc.jpeg.quality", "100");
        } else {
            Companion.buildprop("ro.media.dec.jpeg.memcap", "6000000");
            Companion.buildprop("ro.media.enc.hprof.vid.bps", "6000000");
            Companion.buildprop("ro.media.enc.jpeg.quality", "85");
        }
        }).start();
    }

    /**
     * Qcom tweaks
     */
    public static void qcom_tweaks(boolean s) {
        new Thread(() -> {
        if (s) {
            Companion.buildprop("com.qc.hardware", 1);
            Companion.buildprop("debug.qc.hardware", true);
            Companion.buildprop("debug.qctwa.preservebuf", 1);
            Companion.buildprop("debug.qctwa.statusbar", 1);
        } else {
            Companion.buildprop("com.qc.hardware", 0);
            Companion.buildprop("debug.qc.hardware", false);
            Companion.buildprop("debug.qctwa.preservebuf", 0);
            Companion.buildprop("debug.qctwa.statusbar", 0);
        }
        }).start();
    }

    /**
     * Increments general touch responsiveness
     */
    public static void tweak_touch_controls(boolean s) {
        new Thread(() -> {
        if (s) {
            Companion.buildprop("debug.performance.tuning", 1);
        } else {
            Companion.buildprop("debug.performance.tuning", 0);
        }
        }).start();
    }

    /**
     * Enables 16 bit transparency
     */
    public static void set_16bit_alpha(boolean s) {
        new Thread(() -> {
        if (s) {
            Companion.buildprop("persist.sys.use_16bpp_alpha", 1);
        } else {
            Companion.buildprop("persist.sys.use_16bpp_alpha", 0);
        }
        }).start();
    }

    public static class CPU {
        /**
         * Sets the CPU maximum speed
         */
        public static void set_max(int s) {
            new Thread(() -> {
                int c = HardwareCore.Companion.getCores();
                while (c >= 0) {
                    INSTANCE.run("echo \"" + String.valueOf(s) + "\" > /sys/devices/system/cpu/cpu" + String.valueOf(c) + "/cpufreq/scaling_max_freq");
                    c--;
                }
            }).start();
        }
        public static void set_max(String s) {
            new Thread(() -> {
                int c = HardwareCore.Companion.getCores();
                while (c >= 0) {
                    INSTANCE.run("echo \"" + s + "\" > /sys/devices/system/cpu/cpu" + String.valueOf(c) + "/cpufreq/scaling_max_freq");
                    c--;
                }
            }).start();
        }

        /**
         * Gets the CPU maximum speed
         */
        public static String get_max() {
            return INSTANCE.run("cat /sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq").getStdout().trim();
        }

        /**
         * Gets the CPU maximum speed
         */
        public static String get_max_for(int id) {
            return INSTANCE.run("cat /sys/devices/system/cpu/cpu" + String.valueOf(id) + "/cpufreq/scaling_max_freq").getStdout().trim();
        }

        /**
         * Sets the CPU minimum speed for a core
         */
        public static void set_min(int s) {
            new Thread(() -> {
                int c = HardwareCore.Companion.getCores();
                while (c >= 0) {
                    INSTANCE.run("echo \"" + String.valueOf(s) + "\" > /sys/devices/system/cpu/cpu" + String.valueOf(c--) + "/cpufreq/scaling_min_freq");
                }
            }).start();
        }
        public static void set_min(String s) {
            new Thread(() -> {
                int c = HardwareCore.Companion.getCores();
                while (c >= 0) {
                    INSTANCE.run("echo \"" + s + "\" > /sys/devices/system/cpu/cpu" + String.valueOf(c--) + "/cpufreq/scaling_min_freq");
                }
            }).start();
        }

        /**
         * Gets the CPU minimum speed
         */
        public static String get_min() {
            return INSTANCE.run("cat /sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq").getStdout().trim();
        }

        /**
         * Gets the CPU minimum speed for a core
         */
        public static String get_min_for(int id) {
            return INSTANCE.run("cat /sys/devices/system/cpu/cpu" + String.valueOf(id) + "/cpufreq/scaling_min_freq").getStdout().trim();
        }

        /**
         * Sets the cpu governor
         */
        public static void set_governor(String g) {
            new Thread(() -> {
                int c = HardwareCore.Companion.getCores();
                while (c >= 0) {
                    INSTANCE.run("echo " + g + " > /sys/devices/system/cpu/cpu" + String.valueOf(c--) + "/cpufreq/scaling_governor");
                }
            }).start();
        }

        /**
         * Gets the cpu governor
         */
        public static String get_governor() {
            return INSTANCE.run("cat /sys/devices/system/cpu/cpu0/cpufreq/scaling_governor").getStdout().trim();
        }
    }

    /**
     * Boosts CPU performances
     */
    public static void cpu_boost(boolean s) {
        new Thread(() -> {
        if (s) {
            Companion.buildprop("debug.performance.tuning", 1);
            Companion.sysctl("net.core.optmem_max", 20480);
            Companion.sysctl("net.unix.max_dgram_qlen", 50);
        } else {
            Companion.buildprop("debug.performance.tuning", 0);
            Companion.sysctl("net.core.optmem_max", 14336);
            Companion.sysctl("net.unix.max_dgram_qlen", 40);
        }
        }).start();
    }

    /**
     * Optimizes CPU Usage
     */
    public static void optimize_cpu_usage(boolean s) {
        new Thread(() -> {
        if (s) {
            Companion.buildprop("dalvik.vm.dexopt-flags", "o=y,v=a");
            Companion.buildprop("persist.sys.use_dithering", 1);
            Companion.buildprop("persist.sys.purgeable_assets", 1);
        } else {
            Companion.buildprop("dalvik.vm.dexopt-flags", "o=y,v=n");
            Companion.buildprop("persist.sys.use_dithering", 0);
            Companion.buildprop("persist.sys.purgeable_assets", 0);
        }
        }).start();
    }

    /**
     * Sets runtime execution mode
     */
    public static void set_execution_mode(boolean s) {
        new Thread(() -> {
        if (s) {
            Companion.buildprop("dalvik.vm.execution-mode", "int:fast");
        } else {
            Companion.buildprop("dalvik.vm.execution-mode", "int:portable");
        }
        }).start();
    }

    /**
     * Clears dalvik cache
     */
    public static void clear_dalvik_cache(boolean reboot) {
        new Thread(() -> {
                INSTANCE.mount();
        INSTANCE.run("rm -rf /data/dalvik-cache/*");
        if (reboot) reboot();
        }).start();
    }

    /**
     * Reboots the device
     */
    public static void reboot() {
        INSTANCE.crun("reboot");
    }


    /**
     * Sets RAM Profile
     */
    public static void set_ram_profile(int s) {
        new Thread(() -> {
        switch (s) {
            case 0: //Device default
                break;

            case 1: //Power-saving
                Companion.buildprop("dalvik.vm.lockprof.threshold", 800);
                Companion.buildprop("profiler.debugmonitor", false);
                break;
            case 2: //Smooth
                Companion.buildprop("dalvik.vm.lockprof.threshold", 500);

                break;
            case 3: //Multitasking

                Companion.buildprop("dalvik.vm.lockprof.threshold", 250);

                break;
        }
        }).start();
    }

    /**
     * Sets LMK profile
     */
    public static void lmk(int profile, PreferencesBuilder mBuilder) {
        new Thread(() -> {
            RootFile cost = new RootFile("/sys/module/lowmemorykiller/parameters/cost");
            cost.write("16");

            RootFile debug = new RootFile("/sys/module/lowmemorykiller/parameters/debug_level");
            debug.write("0");

            switch (profile) {
                case 0:
                    String lmk = mBuilder.getString("default_lmk","");
                    if (!lmk.trim().isEmpty()) {
                        INSTANCE.run("echo " + lmk + " > /sys/module/lowmemorykiller/parameters/minfree");
                    }
                    break;
                case 1:
                    INSTANCE.run("echo " + FrameworkSurface.Companion.LMK_VERY_LIGHT + " > /sys/module/lowmemorykiller/parameters/minfree");
                    break;
                case 2:
                    INSTANCE.run("echo " + FrameworkSurface.Companion.LMK_LIGHT + " > /sys/module/lowmemorykiller/parameters/minfree");
                    break;
                case 3:
                    INSTANCE.run("echo " + FrameworkSurface.Companion.LMK_NORMAL + " > /sys/module/lowmemorykiller/parameters/minfree");
                    break;
                case 4:
                    INSTANCE.run("echo " + FrameworkSurface.Companion.LMK_AGGRESSIVE + " > /sys/module/lowmemorykiller/parameters/minfree");
                    break;
                case 5:
                    INSTANCE.run("echo " + FrameworkSurface.Companion.LMK_VERY_AGGRESSIVE + " > /sys/module/lowmemorykiller/parameters/minfree");
                    break;
                case 6:
                    INSTANCE.run("echo " + FrameworkSurface.Companion.LMK_INSANE + " > /sys/module/lowmemorykiller/parameters/minfree");
                    break;
            }
        }).start();
    }


    /**
     * Enables zram service
     */
    public static void set_zram(boolean s) {
        new Thread(() -> {
        if (s) {
            Companion.buildprop("persist.service.zram", 1);
        } else {
            Companion.buildprop("persist.service.zram", 0);
        }
        }).start();
    }

    /**
     * Launcher dedicated LMK propriety for devices with KitKat
     */
    public static void home_app_adj(boolean s) {
        new Thread(() -> {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                if (s) {
                    Companion.buildprop("ro.HOME_APP_ADJ", 1);
                } else {
                    Companion.buildprop("ro.HOME_APP_ADJ", 0);
                }
            }
        }).start();
    }

    /**
     * Sets maximum event number
     */
    public static void boost_max_events(boolean s) {
        new Thread(() -> {
            int cores = HardwareCore.Companion.getCores();
            if (s) {
                Companion.buildprop("windowsmgr.max_events_per_sec", 500);
                Companion.buildprop("ro.max.fling_velocity", 30000);
            } else {
                Companion.remove_buildprop("windowsmgr.max_events_per_sec");
                Companion.remove_buildprop("ro.max.fling_velocity");
                Companion.remove_buildprop("ro.min.fling_velocity");
            }
        }).start();
    }

    /**
     * Memory release
     */
    public static void set_memory_release(boolean s) {
        new Thread(() -> {
        if (s) {
            Companion.buildprop("dalvik.vm.heapminfree", "2m");
        } else {
            Companion.buildprop("dalvik.vm.heapminfree", "512k");
        }
        }).start();
    }

    /**
     * Multitasking ad-hoc method
     */
    public static void multitasking_patches(boolean s) {
        new Thread(() -> {
        if (s) {
            Companion.buildprop("persist.sys.purgeable_assets", 1);
        } else {
            Companion.buildprop("persist.sys.purgeable_assets", 0);
        }
        }).start();
    }

    /**
     * Sets heapgrowthlimit
     */
    public static void set_heapgrowthlimit(String value) {
        new Thread(() -> Companion.buildprop("dalvik.vm.heapgrowthlimit", value)).start();
    }

    /**
     * Sets heapsize
     */
    public static void set_heapsize(String value) {
        new Thread(() -> Companion.buildprop("dalvik.vm.heapsize", value)).start();
    }

    /**
     * Sets heapsize
     */
    public static void set_maxfree(String value) {
        new Thread(() -> Companion.buildprop("dalvik.vm.maxfree", value)).start();
    }

    /**
     * Sets heapsize
     */
    public static void set_minfree(String value) {
        new Thread(() -> Companion.buildprop("dalvik.vm.minfree", value)).start();
    }

    /**
     * Sets heapsize
     */
    public static void set_heaptargetutilization(String value) {
        new Thread(() -> Companion.buildprop("dalvik.vm.heaptargetutilization", value)).start();
    }

    /**
     * Sets dexoptflags
     */
    public static void set_flags(String flags) {
        new Thread(() -> Companion.buildprop("dalvik.vm.dexopt-flags", flags)).start();
    }

    /**
     * Sets wifi on interval
     */
    public static void set_wifi_scan(int s) {
        new Thread(() -> Companion.buildprop("wifi.supplicant_scan_interval", s)).start();
    }

    /**
     * Disables power collapse
     */
    public static void disble_power_collapse(boolean s) {
        new Thread(() -> {
        if (s) {
            Companion.buildprop("ro.ril.disable.power.collapse", 0);
        } else {
            Companion.buildprop("ro.ril.disable.power.collapse", 1);
        }
        }).start();
    }


    /**
     * Disabled bytecode verification
     */
    public static void disable_bytecodecheck(boolean s) {
        new Thread(() -> {
        if (s) {
            Companion.buildprop("dalvik.vm.verify-bytecode", false);
        } else {
            Companion.buildprop("dalvik.vm.verify-bytecode", true);
        }
        }).start();
    }

    /**
     * Wakes the device up pressing volume + or -
     */
    public static void set_wakeup_method_volume(boolean s) {
        new Thread(() -> {
        if (s) {
            Companion.buildprop("ro.config.hwfeature_wakeupkey", 1);
        } else {
            Companion.buildprop("ro.config.hwfeature_wakeupkey", 0);
        }
        }).start();
    }

    /**
     * Enables permanent back button illumination
     */
    public static void set_always_on_backlights(boolean s) {
        new Thread(() -> {
        if (s) {
            SETTINGS.INSTANCE.put("system", "button_key_light", "-1");
        } else {
            SETTINGS.INSTANCE.put("system", "button_key_light", "0");
        }
        }).start();
    }

    /**
     * Disables some internal services to get more battery
     */
    public static void set_internal_async_battery(boolean s) {
        new Thread(() -> {
        if (s) {
            Shell.SU.run("echo \"500\" > /proc/sys/vm/dirty_expire_centisecs");
            Shell.SU.run("echo \"1000\" > /proc/sys/vm/dirty_writeback_centisecs");
            Companion.buildprop("logcat.live", "disable");
            Companion.buildprop("ro.ril.disable.power.collapse", 1);
            Companion.buildprop("profiler.debugmonitor", false);
            Companion.buildprop("profiler.launch", false);
            Companion.buildprop("ro.mot.eri.losalert.delay", 1000);
        } else {
            Companion.buildprop("logcat.live", "enable");
            Companion.buildprop("ro.ril.disable.power.collapse", 0);
            Companion.buildprop("debugtool.anrhistory", 1);
            Companion.buildprop("profiler.launch", true);
        }
        }).start();
    }

    /**
     * Smooth UI
     */
    public static void set_ui_smooth(boolean s) {
        new Thread(() -> {
        if (s) {
            Companion.buildprop("touch.pressure.scale", "0.1");
        } else {

        }
        }).start();
    }

    public static class CONNECTION {

        /**
         * Combined signal
         */
        public static void set_using_combined_signal(boolean s) {
            new Thread(() -> {
            if (s) {
                Companion.buildprop("ro.config.combined_signal", true);
            } else {
                Companion.buildprop("ro.config.combined_signal", false);
            }
            }).start();
        }

        /**
         * Better signal
         */
        public static void set_improved_signal(boolean s) {
            new Thread(() -> {
            if (s) {
                Companion.buildprop("persist.cust.tel.eons", 1);
            } else {
                Companion.buildprop("persist.cust.tel.eons", 0);
            }
            }).start();
        }

        /**
         * Enables fast dormancy
         */
        public static void set_fast_dormancy(boolean s) {
            new Thread(() -> {
            if (s) {
                Companion.buildprop("ro.config.hw_fast_dormancy", 1);
            } else {
                Companion.buildprop("ro.config.hw_fast_dormancy", 0);
            }
            }).start();
        }

        /**
         * Improves buffersizes
         */
        public static void set_big_buffersize(boolean s) {
            new Thread(() -> {
            if (s) {
                Companion.buildprop("net.tcp.buffersize.default", "4096,87380,256960,4096,16384,256960");
                Companion.buildprop("net.tcp.buffersize.wifi", "6144,87380,524288,6144,16384,262144");
                Companion.buildprop("net.tcp.buffersize.umts", "4096,87380,256960,4096,16384,256960");
                Companion.buildprop("net.tcp.buffersize.gprs", "4096,87380,256960,4096,16384,256960");
                Companion.buildprop("net.tcp.buffersize.edge", "4096,87380,256960,4096,16384,256960");
                Companion.buildprop("net.tcp.buffersize.hspa", "6144,87380,524288,6144,16384,262144");
                Companion.buildprop("net.tcp.buffersize.lte", "4096,87380,256960,4096,16384,256960");
                Companion.buildprop("net.tcp.buffersize.hsdpa", "6144,87380,1048576,6144,87380,1048576");
                Companion.buildprop("net.tcp.buffersize.evdo_b", "6144,87380,1048576,6144,87380,1048576");
            } else {
                Companion.buildprop("net.tcp.buffersize.default", "4096,87380,256960,4096,16384,256960");
                Companion.buildprop("net.tcp.buffersize.wifi", "4096,87380,256960,4096,16384,256960");
                Companion.buildprop("net.tcp.buffersize.umts", "4096,87380,256960,4096,16384,256960");
                Companion.buildprop("net.tcp.buffersize.gprs", "4096,87380,256960,4096,16384,256960");
                Companion.buildprop("net.tcp.buffersize.edge", "4096,87380,256960,4096,16384,256960");
                Companion.buildprop("net.tcp.buffersize.hspa", "4096,87380,256960,4096,16384,256960");
                Companion.buildprop("net.tcp.buffersize.lte", "4096,87380,256960,4096,16384,256960");
                Companion.buildprop("net.tcp.buffersize.hsdpa", "4096,87380,256960,4096,16384,256960");
                Companion.buildprop("net.tcp.buffersize.evdo_b", "4096,87380,256960,4096,16384,256960");
            }
            }).start();
        }


        /**
         * Sets Google DNS servers as default
         */
        public static void set_google_dns(boolean s) {
            new Thread(() -> {
            if (s) {
                Companion.buildprop("network.dns1", "8.8.8.8");
                Companion.buildprop("network.rmnet0.dns1", "8.8.8.8");
                Companion.buildprop("network.dns2", "8.8.4.4");
                Companion.buildprop("network.rmnet0.dns1", "8.8.4.4");
            } else {

            }
            }).start();
        }

        /**
         * Sets TCP algoritm
         */
        public static void tcp_algorithm(boolean s) {
            new Thread(() -> {
            if (s) {
                Companion.buildprop("network.ipv4.tcp_congestion_control", "cubic");
            } else {
                Companion.buildprop("network.ipv4.tcp_congestion_control", "reno");
            }
            }).start();
        }

        /**
         * Voice call tweaks
         */
        public static void tweak_voice_calls(boolean s) {
            new Thread(() -> {
            if (s) {
                Companion.buildprop("persist.dbg.ims_volte_enable", 1);
                Companion.buildprop("persist.dbg.volte_avail_ovr", 1);
                Companion.buildprop("persist.dbg.vt_avail_ovr", 0);
                Companion.buildprop("persist.data.iwlan.enable", true);
                Companion.buildprop("persist.dbg.wfc_avail_ovr", 0);
            } else {
                Companion.buildprop("persist.dbg.ims_volte_enable", 0);
                Companion.buildprop("persist.dbg.volte_avail_ovr", 0);
                Companion.buildprop("persist.dbg.vt_avail_ovr", 0);
                Companion.buildprop("persist.data.iwlan.enable", false);
                Companion.buildprop("persist.dbg.wfc_avail_ovr", 0);
            }
            }).start();
        }

        /**
         * Tweaks mobile connections
         */
        public static void tweak_mobile_connection(boolean s) {
            new Thread(() -> {
            if (s) {
                Companion.buildprop("ro.ril.hsxpa", String.valueOf("2"));
                Companion.buildprop("ro.ril.gprsclass", String.valueOf("12"));
                Companion.buildprop("ro.ril.hep", String.valueOf("1"));
                Companion.buildprop("ro.ril.hsdpa.category", String.valueOf("10"));
                Companion.buildprop("ro.ril.hsupa.category", String.valueOf("6"));
                Companion.buildprop("persist.cust.tel.eons", String.valueOf("1"));
                Companion.buildprop("ro.ril.enable.3g.prefix", String.valueOf("1"));
                if (Build.BRAND.contains("htc")) {
                    Companion.buildprop("ro.ril.htcmaskw1.bitmask", String.valueOf("4294967295"));
                    Companion.buildprop("ro.ril.htcmaskw1", String.valueOf("14449"));
                }
                Companion.buildprop("ro.ril.def.agps.mode", String.valueOf("2"));
                Companion.buildprop("ro.ril.enable.sdr", String.valueOf("1"));
                Companion.buildprop("ro.ril.enable.gea3", String.valueOf("1"));
                Companion.buildprop("ro.ril.enable.fd.plmn.prefix", String.valueOf("23402,23410,23411"));
                Companion.buildprop("ro.ril.enable.a52", String.valueOf("1"));
                Companion.buildprop("ro.ril.enable.a53", String.valueOf("1"));
                Companion.buildprop("ro.ril.enable.dtm", String.valueOf("1"));
                Companion.buildprop("ro.semc.enable", String.valueOf("1"));
                Companion.buildprop("ro.ril.enable.a52", String.valueOf("1"));
            } else {
                //buildprop("", );
            }
            }).start();
        }

        /**
         * Signal general tweaks
         */
        public static void remove_carrier_limits(boolean s) {
            new Thread(() -> {
            if (s) {
                Companion.buildprop("ro.config.vc_call_steps", 20);
            } else {}
            }).start();
        }

        /**
         * Enables wideband
         */
        public static void enable_wideband(boolean s) {
            new Thread(() -> {
            if (s) {
                Companion.buildprop("ro.ril.enable.amr.wideband", 1);
            } else {
                Companion.buildprop("ro.ril.enable.amr.wideband", 0);
            }
            }).start();
        }

        /*
         *
         *   ro.ril.hsxpa=3
         *   ro.ril.gprsclass=10
         *   ro.ril.hep=1
         *   ro.ril.enable.dtm=1
         *   ro.ril.hsdpa.category=12
         *   ro.ril.enable.a53=1
         *   ro.ril.enable.3g.prefix=1
         *   ro.ril.htcmaskw1.bitmask=4294967295
         *   ro.ril.htcmaskw1=14449
         *   ro.ril.hsupa.category=7
         *   ro.ril.hsdpa.category=10
         *   ro.ril.enable.a52=1
         *   ro.ril.set.mtu1472=1
         *   persist.cust.tel.eons=1
         *   ro.config.hw_fast_dormancy=1
         *   persist.data_netmgrd_mtu=1482
         *   persist.data_netmgrd_nint=8
         *   ro.use_data_netmgrd=true
         *   ro.ril.enable.dtm=1
         *   ro.ril.def.agps.mode=2
         *   ro.ril.def.agps.feature=1
         *   ro.ril.enable.gea3=1
         *   ro.ril.enable.fd.plmn.prefix=23402,23410,23411
         *
         * */



    }

    /**
     * Disable default error reporting
     */
    public static void disable_error_logging(boolean s) {
        new Thread(() -> {
        if (s) {
            Companion.buildprop("profiler.force_disable_ulog", 1);
            Companion.buildprop("profiler.force_disable_err_rpt", 1);
        } else {
            Companion.buildprop("profiler.force_disable_ulog", 0);
            Companion.buildprop("profiler.force_disable_err_rpt", 0);
        }
        }).start();
    }


    /**
     * Optimizes battery settings
     */
    public static void optimize_battery_services(boolean s) {
        new Thread(() -> {
        if (s) {
            Companion.buildprop("ro.ril.power_collapse", 1);
            Companion.buildprop("pm.sleep_mode", 1);
            Companion.buildprop("persist.sys.use_dithering", 0);
        } else {
            Companion.buildprop("ro.ril.power_collapse", 0);
            Companion.buildprop("pm.sleep_mode", 0);
            Companion.buildprop("persist.sys.use_dithering", 1);
        }
        }).start();
    }

    /**
     * Enables quickpoweron
     */
    public static void quickpoweron(boolean s) {
        new Thread(() -> {
        if (s) {
            Companion.buildprop("ro.config.hw_quickpoweron", true);
        } else {
            Companion.buildprop("ro.config.hw_quickpoweron", false);
        }
        }).start();
    }

    /**
     * Disables data sent
     */
    public static void disable_error_sent(boolean s) {
        new Thread(() -> {
        if (s) {
            Companion.buildprop("ro.config.nocheckin", 1);
        } else {
            Companion.buildprop("ro.config.nocheckin", 0);
        }
        }).start();
    }

    /**
     * Sets secure
     */
    public static void set_secure(boolean s) {
        new Thread(() -> {
        if (s) {
            Companion.buildprop("ro.secure", 1);
        } else {
            Companion.buildprop("ro.secure", 0);
        }
        }).start();
    }

    /**
     * Enables fast charging
     */
    public static void set_fast_charging(boolean s) {
        new Thread(() -> {
        if (s) {
            SETTINGS.INSTANCE.put(FrameworkSurface.Companion.SYSTEM, "adaptive_fast_charging", 1);
        } else {
            SETTINGS.INSTANCE.put(FrameworkSurface.Companion.SYSTEM, "adaptive_fast_charging", 0);
        }
        }).start();
    }

    /**
     * Gets the current fast charging status
     */
    public static boolean get_fast_charging() {
        return SETTINGS.INSTANCE.get(FrameworkSurface.Companion.SYSTEM, "adaptive_fast_charging").equals("1");
    }

    /**
     * Enables fast charging
     */
    public static void wireless_fast_charging(boolean s) {
        new Thread(() -> {
        if (s) {
            SETTINGS.INSTANCE.put(FrameworkSurface.Companion.SYSTEM, "wireless_fast_charging", 1);
        } else {
            SETTINGS.INSTANCE.put(FrameworkSurface.Companion.SYSTEM, "wireless_fast_charging", 0);
        }
        }).start();
    }


    /**
     * Gets the current wireless fast charging status
     */
    public static boolean get_wireless_fast_charging() {
        return SETTINGS.INSTANCE.get(FrameworkSurface.Companion.SYSTEM, "wireless_fast_charging").equals("1");
    }

    /**
     * Enables sleep mode
     */
    public static void set_sleep_mode(boolean s) {
        new Thread(() -> {
        if (s) {
            Companion.buildprop("pm.sleep_mode", 1);
        } else {
            Companion.buildprop("pm.sleep_mode", 0);
        }
        }).start();
    }

    /**
     * Sets ADB notification visbility state
     */
    public static void set_adb_notification(boolean s) {
        new Thread(() -> {
        if (s) {
            Companion.buildprop("persist.adb.notify", 1);
        } else {
            Companion.buildprop("persist.adb.notify", 0);
        }
        }).start();
    }

    /**
     * Disables google anr history
     */
    public static void disable_anr_history(boolean s) {
        new Thread(() -> {
        if (s) {
            Companion.buildprop("debugtool.anrhistory", 1);
        } else {
            Companion.buildprop("debugtool.anrhistory", 0);
        }
        }).start();
    }

    /**
     * Optimized debug environment
     */
    public static void set_debug_optimization(boolean s) {
        new Thread(() -> {
        if (s) {
            Companion.buildprop("debug.egl.hw", 1);
        } else {
            Companion.buildprop("debug.egl.hw", 0);
        }
        }).start();
    }

    /**
     * Sets kernel panic delay
     */
    public static void set_kernelpanic(int s) {
        new Thread(() -> Companion.sysctl("kernel.panic", s)).start();
    }

    /**
     * Tweaks general kernel parameters
     */
    public static void apply_kernel_tweaks(boolean s) {
        new Thread(() -> {
        if (s) {
            Companion.buildprop("vm.vfs_cache_pressure", "50");
            Companion.buildprop("vm.swappiness", "0");
            Companion.buildprop("vm.dirty_ratio", "90");
            Companion.buildprop("vm.dirty_background_ratio", "70");
            Companion.buildprop("vm.panic_on_oom", "2");
            Companion.buildprop("fs.file-max", "65536");
        } else {
            Companion.buildprop("vm.vfs_cache_pressure", "100");
        }
        }).start();
    }

    /**
     * Sleepers optimization
     */
    public static void kernel_sleepers_optimization() {
        new Thread(() -> INSTANCE.run("if [ -e \"/sys/kernel/debug/sched_features\" ]; then\n" +
                "echo \"NO_AFFINE_WAKEUPS\" >> /sys/kernel/debug/sched_features\n" +
                "fi\n" +
                "if [ -e \"/sys/kernel/debug/sched_features\" ]; then\n" +
                "echo \"NO_ARCH_POWER\" >> /sys/kernel/debug/sched_features\n" +
                "fi\n" +
                "if [ -e \"/sys/kernel/debug/sched_features\" ]; then\n" +
                "echo \"NO_CACHE_HOT_BUDDY\" >> /sys/kernel/debug/sched_features\n" +
                "fi\n" +
                "if [ -e \"/sys/kernel/debug/sched_features\" ]; then\n" +
                "echo \"NO_DOUBLE_TICK\" >> /sys/kernel/debug/sched_features\n" +
                "fi\n" +
                "if [ -e \"/sys/kernel/debug/sched_features\" ]; then\n" +
                "echo \"NO_FORCE_SD_OVERLAP\" >> /sys/kernel/debug/sched_features\n" +
                "fi\n" +
                "if [ -e \"/sys/kernel/debug/sched_features\" ]; then\n" +
                "echo \"NO_GENTLE_FAIR_SLEEPERS\" >> /sys/kernel/debug/sched_features\n" +
                "fi\n" +
                "if [ -e \"/sys/kernel/debug/sched_features\" ]; then\n" +
                "echo \"NO_HRTICK\" >> /sys/kernel/debug/sched_features\n" +
                "fi\n" +
                "if [ -e \"/sys/kernel/debug/sched_features\" ]; then\n" +
                "echo \"NO_LAST_BUDDY\" >> /sys/kernel/debug/sched_features\n" +
                "fi\n" +
                "if [ -e \"/sys/kernel/debug/sched_features\" ]; then\n" +
                "echo \"NO_LB_BIAS\" >> /sys/kernel/debug/sched_features\n" +
                "fi\n" +
                "if [ -e \"/sys/kernel/debug/sched_features\" ]; then\n" +
                "echo \"NO_LB_MIN\" >> /sys/kernel/debug/sched_features\n" +
                "fi\n" +
                "if [ -e \"/sys/kernel/debug/sched_features\" ]; then\n" +
                "echo \"NO_NEW_FAIR_SLEEPERS\" >> /sys/kernel/debug/sched_features\n" +
                "fi\n" +
                "if [ -e \"/sys/kernel/debug/sched_features\" ]; then\n" +
                "echo \"NO_NEXT_BUDDY\" >> /sys/kernel/debug/sched_features\n" +
                "fi\n" +
                "if [ -e \"/sys/kernel/debug/sched_features\" ]; then\n" +
                "echo \"NO_NONTASK_POWER\" >> /sys/kernel/debug/sched_features\n" +
                "fi\n" +
                "if [ -e \"/sys/kernel/debug/sched_features\" ]; then\n" +
                "echo \"NO_NORMALIZED_SLEEPERS\" >> /sys/kernel/debug/sched_features\n" +
                "fi\n" +
                "if [ -e \"/sys/kernel/debug/sched_features\" ]; then\n" +
                "echo \"NO_OWNER_SPIN\" >> /sys/kernel/debug/sched_features\n" +
                "fi\n" +
                "if [ -e \"/sys/kernel/debug/sched_features\" ]; then\n" +
                "echo \"NO_RT_RUNTIME_SHARE\" >> /sys/kernel/debug/sched_features\n" +
                "fi\n" +
                "if [ -e \"/sys/kernel/debug/sched_features\" ]; then\n" +
                "echo \"NO_START_DEBIT\" >> /sys/kernel/debug/sched_features\n" +
                "fi\n" +
                "if [ -e \"/sys/kernel/debug/sched_features\" ]; then\n" +
                "echo \"NO_TTWU_QUEUE\" >> /sys/kernel/debug/sched_features\n" +
                "fi")).start();
    }

    /**
     * Disables kernel JNI checks
     */
    public static void disable_kernel_jni_check(boolean s) {
        new Thread(() -> {
        if (s) {
            Companion.sysctl("ro.kernel.checkjni", 0);
            Companion.buildprop("dalvik.vm.checkjni", 0);
            Companion.sysctl("ro.kernel.android.checkjni", 0);
        } else {
            Companion.sysctl("ro.kernel.checkjni", 1);
            Companion.buildprop("dalvik.vm.checkjni", 1);
            Companion.sysctl("ro.kernel.android.checkjni", 1);
        }
        }).start();
    }

    /**
     * Sets kernel OOPS behavior
     */
    public static void enable_reboot_on_oops(boolean s) {
        new Thread(() -> {
        if (s) {
            Companion.sysctl("kernel.panic_on_oops", 1);
        } else {
            Companion.sysctl("kernel.panic_on_oops", 0);
        }
        }).start();
    }


    /**
     * Optimizes entropy adjusting kernel values
     */
    public static void enable_entropy_optimization(boolean s) {
        new Thread(() -> {
        if (s) {
            Companion.sysctl("kernel.shmall", 16777216);
            Companion.sysctl("kernel.shmmax", 268435456);
            Companion.sysctl("kernel.msgmni", 4086);
            Companion.sysctl("kernel.msgmax", 128000);
        } else {
            Companion.sysctl("kernel.shmall", 13421772);
            Companion.sysctl("kernel.shmmax", 236435456);
            Companion.sysctl("kernel.msgmni", 2048);
            Companion.sysctl("kernel.msgmax", 64000);
        }
        }).start();
    }

    /**
     * Sents the maximum number of threads per-app
     */
    public static void set_max_threads_number(int s) {
        new Thread(() -> Companion.sysctl("kernel.threads-max", s)).start();
    }

    /**
     * Disables bootanimation
     */
    public static void disable_bootanimation(boolean s) {
        new Thread(() -> {
        if (s) {
            Companion.buildprop("debug.sf.nobootanimation", 1);
        } else {
            Companion.buildprop("debug.sf.nobootanimation", 0);
        }
        }).start();
    }

    /**
     * Enables mock location
     */
    public static void set_mock_location(boolean s) {
        new Thread(() -> {
        if (s) {
            Companion.buildprop("ro.allow.mock.location", 1);
            SETTINGS.INSTANCE.put(FrameworkSurface.Companion.SECURE, "mock_location", 1);
        } else {
            Companion.buildprop("ro.allow.mock.location", 0);
            SETTINGS.INSTANCE.put(FrameworkSurface.Companion.SECURE, "mock_location", 0);
        }
        }).start();
    }

    /**
     * Disables call ring delay
     */
    public static void disable_call_delay(boolean s) {
        new Thread(() -> {
        if (s) {
            Companion.buildprop("ro.telephony.call_ring.delay", 0);
        } else {
            Companion.buildprop("ro.telephony.call_ring.delay", 1);
        }
        }).start();
    }

    /**
     * Disables small black screen after calls
     */
    public static void disable_black_screen_after_calls(boolean s) {
        new Thread(() -> {
        if (s) {
            Companion.buildprop("ro.lge.proximity.delay", 25);
            Companion.buildprop("mot.proximity.delay", 25);
        } else {
            Companion.buildprop("ro.lge.proximity.delay", 70);
            Companion.buildprop("mot.proximity.delay", 70);
        }
        }).start();
    }


    /**
     * Sets the maximum amount of touches
     */
    public static void set_max_touch(int s) {
        new Thread(() -> Companion.buildprop("ro.product.max_num_touch", s)).start();
    }

    /**
     * Launcher/Lockscreen rotation
     */
    public static void set_lockscreen_rotation(boolean s) {
        new Thread(() -> {
        if (s) {
            Companion.buildprop("log.tag.launcher_force_rotate", "VERBOSE");
            Companion.buildprop("lockscreen.rot_override", true);
        } else {
            Companion.buildprop("log.tag.launcher_force_rotate", "ERROR");
            Companion.buildprop("lockscreen.rot_override", false);
        }
        }).start();
    }


    /**
     * Enable 180 rotation
     * 0 = Landscape, 90 = Portrait, 180 = Reverse Landscape, 270 = Reverse Portrait
     */
    public static void set_180_rot(boolean s) {
        new Thread(() -> {
        if (s) {
            Companion.buildprop("windowsmgr.support_rotation_270", true);
            Companion.buildprop("windowsmgr.support_rotation_180", true);
            Companion.buildprop("ro.sf.hwrotation", 180);
        } else {
            Companion.buildprop("windowsmgr.support_rotation_270", false);
            Companion.buildprop("windowsmgr.support_rotation_180", false);
            Companion.buildprop("ro.sf.hwrotation", 270);
        }
        }).start();
    }

    /**
     * Hardware-enabled rendering
     */
    public static void hardware_rendering(boolean s) {
        new Thread(() -> {
        if (s) {
            Companion.buildprop("video.accelerate.hw", 1);
        } else {
            Companion.buildprop("video.accelerate.hw", 0);
        }
        }).start();
    }

    /**
     * Gets kernel details
     */
    public static String kernel_info() {
        String details = INSTANCE.run("uname -a").getStdout();
        if (details.trim().length() < 8) return "Linux Kernel on Android " + Build.VERSION.SDK_INT;
        return details;
    }

    /**
     * Gets short details
     */
    public static String restricted_kernel_info() {
        return INSTANCE.run("uname -sr").getStdout().trim();
    }

    /**
     * Return the network KEY
     */
    public static String get_hostname() {
        return INSTANCE.run("getprop network.hostname").getStdout()
                .trim();
    }


    /**
     * Sets the network KEY
     */
    public static void set_hostname(String h) {
        new Thread(() -> {
            Companion.buildprop("network.hostname", h);
            Companion.setprop("network.hostname", h);
        }).start();
    }

    /**
     * Enable OEM unlock
     */
    public static void set_oem_unlocking(boolean s) {
        new Thread(() -> {
        if (s) {
            Companion.buildprop("sys.oem_unlock_allowed", 1);
        } else {
            Companion.buildprop("sys.oem_unlock_allowed", 0);
        }
        }).start();
    }

    /**
     * Dump OOM tasksk
     */
    public static void dump_oom_tasks(boolean s) {
        new Thread(() -> {
        if (s) {
            Companion.buildprop("vm.oom_dump_tasks", 1);
        } else {
            Companion.buildprop("vm.oom_dump_tasks", 0);
        }
        }).start();
    }

    /**
     * Sets the adb shell as secure
     */
    public static void secure_adb_shell(boolean s) {
        new Thread(() -> {
        if (s) {
            Companion.buildprop("ro.adb.secure", 1);
        } else {
            Companion.buildprop("ro.adb.secure", 0);
        }
        }).start();
    }

    /**
     * Allows APK files installation
     */
    public static void allow_unknown_sources(boolean s) {
        new Thread(() -> {
        if (s) {
            SETTINGS.INSTANCE.put(FrameworkSurface.Companion.SECURE, "install_non_market_apps", 1);
        } else {
            SETTINGS.INSTANCE.put(FrameworkSurface.Companion.SECURE, "install_non_market_apps", 0);
        }
        }).start();
    }

    /**
     * Secures adb installations by checking APK files
     */
    public static void secure_sources_usb_adb(boolean s) {
        new Thread(() -> {
        if (s) {
            SETTINGS.INSTANCE.put(FrameworkSurface.Companion.SECURE, "verifier_verify_adb_installs", 1);
        } else {
            SETTINGS.INSTANCE.put(FrameworkSurface.Companion.SECURE, "verifier_verify_adb_installs", 0);
        }
        }).start();
    }

    /**
     * Remove battery stats
     */
    public static void erase_battery_stats() {
        RootFile batterystats = new RootFile("/data/system/batterystats.bin");
        batterystats.delete();
    }

    /**
     * Disables battery drain
     */
    public static void disable_google_battery_drain(boolean s) {
        new Thread(() -> {
            if (s) {

            }
        }).start();
    }
}