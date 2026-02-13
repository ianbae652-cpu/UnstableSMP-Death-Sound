package com.ian.nearbydeath;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NearbyDeathMod implements ModInitializer {

    private static final double RADIUS = 150;

    // 플레이어 이전 생존 상태 저장
    private final Map<UUID, Boolean> aliveMap = new HashMap<>();

    @Override
    public void onInitialize() {

        ServerTickEvents.END_SERVER_TICK.register(this::onServerTick);
    }

    private void onServerTick(MinecraftServer server) {

        for (ServerWorld world : server.getWorlds()) {

            for (ServerPlayerEntity player : world.getPlayers()) {

                UUID id = player.getUuid();
                boolean alive = player.isAlive();

                Boolean wasAlive = aliveMap.get(id);

                // 처음 등록
                if (wasAlive == null) {
                    aliveMap.put(id, alive);
                    continue;
                }

                // 죽은 순간 감지
                if (wasAlive && !alive) {

                    double x = player.getX();
                    double y = player.getY();
                    double z = player.getZ();

                    for (ServerPlayerEntity nearby : world.getPlayers()) {

                        double dx = nearby.getX() - x;
                        double dy = nearby.getY() - y;
                        double dz = nearby.getZ() - z;

                        if (dx * dx + dy * dy + dz * dz <= RADIUS * RADIUS) {

                            world.playSound(
                                    null,
                                    nearby.getBlockPos(),
                                    SoundEvents.ENTITY_WITHER_SPAWN,
                                    SoundCategory.MASTER,
                                    1F,
                                    1F
                            );
                        }
                    }
                }

                aliveMap.put(id, alive);
            }
        }
    }
}