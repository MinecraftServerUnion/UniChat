package dev.onelili.unichat.velocity.util;

import com.github.retrooper.packetevents.protocol.component.ComponentType;
import com.github.retrooper.packetevents.protocol.component.ComponentTypes;
import com.github.retrooper.packetevents.protocol.component.PatchableComponentMap;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.nbt.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.retrooper.packetevents.adventure.serializer.gson.impl.GsonDataComponentValueConverterProvider;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.DataComponentValue;
import net.kyori.adventure.text.serializer.gson.GsonDataComponentValue;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Utils {
    public static @Nonnull JsonObject getNbtCompoundJson(@Nonnull NBTCompound nbt) {
        JsonObject ret = new JsonObject();

        nbt.getTags().forEach((key, subNbt) -> {
            if(subNbt.getType().equals(NBTType.COMPOUND))
                ret.add(key, getNbtCompoundJson((NBTCompound) subNbt));
            else if(subNbt.getType().equals(NBTType.LIST))
                ret.add(key, getNbtListJson((NBTList<?>) subNbt));
            else if(subNbt.getType().equals(NBTType.BYTE))
                ret.addProperty(key, ((NBTByte) subNbt).getAsByte());
            else if(subNbt.getType().equals(NBTType.BYTE_ARRAY)) {
                JsonArray array = new JsonArray();
                for(byte value : ((NBTByteArray) subNbt).getValue())
                    array.add(value);
                ret.add(key, array);
            }
            // TODO: 把每个nbttype情况都处理下, 大部分可以参考上面
        });

        return ret;
    }

    @SuppressWarnings("NullableProblems")
    public static @Nonnull JsonArray getNbtListJson(@Nonnull NBTList<? extends NBT> nbt) {
        JsonArray ret = new JsonArray();
        nbt.getTags().forEach(subNbt -> {
            // TODO:上面那块写好之后，这里照搬，并把处理后的subNbt加入到ret里面
        });

        return ret;
    }

    public Map<Key, DataComponentValue> getNMSItemStackDataComponents(ItemStack item) {
        if (item.is(ItemTypes.AIR)) {
            return Collections.emptyMap();
        }
        Map<Key, DataComponentValue> convertedComponents = new ConcurrentHashMap<>();
        for (Map.Entry<ComponentType<?>, ?> entry : item.getComponents().getBase().entrySet()) {
            ComponentType<?> type = entry.getKey();
            Object optValue = entry.getValue();
            Key key = type.getName().key();
            if (optValue instanceof Component component) {
//                Object nativeJsonElement = codec.encodeStart(registryAccess.a(JsonOps.INSTANCE), optValue.get()).getOrThrow();
//                JsonElement jsonElement = NativeJsonConverter.fromNative(nativeJsonElement);
                JsonObject json = new JsonObject();
                // TODO
                DataComponentValue value = GsonDataComponentValue.gsonDataComponentValue(json);
                convertedComponents.put(key, value);
            } else {
                convertedComponents.put(key, DataComponentValue.removed());
            }
        }
        return convertedComponents;
    }
}
