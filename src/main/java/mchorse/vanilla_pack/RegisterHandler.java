package mchorse.vanilla_pack;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import mchorse.metamorph.Metamorph;
import mchorse.metamorph.api.MorphManager;
import mchorse.metamorph.api.MorphSettings;
import mchorse.metamorph.api.abilities.IAbility;
import mchorse.metamorph.api.abilities.IAction;
import mchorse.metamorph.api.abilities.IAttackAbility;
import mchorse.metamorph.api.events.RegisterBlacklistEvent;
import mchorse.metamorph.api.events.RegisterSettingsEvent;
import mchorse.metamorph.api.json.MorphSettingsAdapter;
import mchorse.vanilla_pack.abilities.Climb;
import mchorse.vanilla_pack.abilities.FireProof;
import mchorse.vanilla_pack.abilities.Fly;
import mchorse.vanilla_pack.abilities.Glide;
import mchorse.vanilla_pack.abilities.Hungerless;
import mchorse.vanilla_pack.abilities.Jumping;
import mchorse.vanilla_pack.abilities.NightVision;
import mchorse.vanilla_pack.abilities.PreventFall;
import mchorse.vanilla_pack.abilities.SnowWalk;
import mchorse.vanilla_pack.abilities.SunAllergy;
import mchorse.vanilla_pack.abilities.Swim;
import mchorse.vanilla_pack.abilities.WaterAllergy;
import mchorse.vanilla_pack.abilities.WaterBreath;
import mchorse.vanilla_pack.actions.Endermite;
import mchorse.vanilla_pack.actions.Explode;
import mchorse.vanilla_pack.actions.FireBreath;
import mchorse.vanilla_pack.actions.Fireball;
import mchorse.vanilla_pack.actions.Jump;
import mchorse.vanilla_pack.actions.Potions;
import mchorse.vanilla_pack.actions.ShulkerBullet;
import mchorse.vanilla_pack.actions.Sliverfish;
import mchorse.vanilla_pack.actions.SmallFireball;
import mchorse.vanilla_pack.actions.Snowball;
import mchorse.vanilla_pack.actions.Spit;
import mchorse.vanilla_pack.actions.Teleport;
import mchorse.vanilla_pack.attacks.KnockbackAttack;
import mchorse.vanilla_pack.attacks.PoisonAttack;
import mchorse.vanilla_pack.attacks.WitherAttack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Register handler 
 * 
 * This event handler is responsible for 
 */
public class RegisterHandler
{
    /**
     * GSON instance that is responsible for deserializing morph settings
     */
    private final static Gson GSON = new GsonBuilder().registerTypeAdapter(MorphSettings.class, new MorphSettingsAdapter()).create();

    /**
     * Register Metamorph's supplied abilities, actions and attacks. 
     */
    public static void registerAbilities(MorphManager manager)
    {
        /* Define shortcuts */
        Map<String, IAbility> abilities = manager.abilities;
        Map<String, IAttackAbility> attacks = manager.attacks;
        Map<String, IAction> actions = manager.actions;

        /* Register default abilities */
        abilities.put("climb", new Climb());
        abilities.put("fire_proof", new FireProof());
        abilities.put("fly", new Fly());
        abilities.put("glide", new Glide());
        abilities.put("hungerless", new Hungerless());
        abilities.put("jumping", new Jumping());
        abilities.put("night_vision", new NightVision());
        abilities.put("prevent_fall", new PreventFall());
        abilities.put("snow_walk", new SnowWalk());
        abilities.put("sun_allergy", new SunAllergy());
        abilities.put("swim", new Swim());
        abilities.put("water_allergy", new WaterAllergy());
        abilities.put("water_breath", new WaterBreath());

        /* Register default actions */
        actions.put("endermite", new Endermite());
        actions.put("explode", new Explode());
        actions.put("fireball", new Fireball());
        actions.put("fire_breath", new FireBreath());
        actions.put("jump", new Jump());
        actions.put("potions", new Potions());
        actions.put("shulker_bullet", new ShulkerBullet());
        actions.put("silverfish", new Sliverfish());
        actions.put("small_fireball", new SmallFireball());
        actions.put("snowball", new Snowball());
        actions.put("spit", new Spit());
        actions.put("teleport", new Teleport());

        /* Register default attacks */
        attacks.put("poison", new PoisonAttack());
        attacks.put("wither", new WitherAttack());
        attacks.put("knockback", new KnockbackAttack());
    }

    /**
     * Register morph settings from default morphs configuration that 
     * comes with Metamorph and user configuration file 
     */
    @SubscribeEvent
    public void onSettingsReload(RegisterSettingsEvent event)
    {
        this.loadMorphSettings(event.settings, this.getClass().getClassLoader().getResourceAsStream("assets/metamorph/morphs.json"));
        this.loadMorphSettings(event.settings, Metamorph.proxy.morphs);
    }

    /**
     * Load morph settings into {@link MorphManager} with given {@link File} and 
     * with a try-catch which logs out an error in case of failure.
     */
    private void loadMorphSettings(Map<String, MorphSettings> settings, File config)
    {
        try
        {
            this.loadMorphSettings(settings, new FileInputStream(config));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Load morph settings from {@link InputStream}
     */
    private void loadMorphSettings(Map<String, MorphSettings> settings, InputStream input)
    {
        Scanner scanner = new Scanner(input, "UTF-8");

        @SuppressWarnings("serial")
        Type type = new TypeToken<Map<String, MorphSettings>>()
        {}.getType();

        Map<String, MorphSettings> data = GSON.fromJson(scanner.useDelimiter("\\A").next(), type);

        scanner.close();

        for (Map.Entry<String, MorphSettings> entry : data.entrySet())
        {
            String key = entry.getKey();
            MorphSettings morphSettings = entry.getValue();

            if (settings.containsKey(key))
            {
                settings.get(key).merge(morphSettings);
            }
            else
            {
                settings.put(key, morphSettings);
            }
        }
    }

    /**
     * Registers blacklist
     */
    @SubscribeEvent
    public void onRegisterBlacklist(RegisterBlacklistEvent event)
    {
        event.blacklist.add("metamorph:morph");

        this.loadBlacklist(event.blacklist, Metamorph.proxy.blacklist);
    }

    /**
     * Load user provided blacklist using the safe way.
     */
    private void loadBlacklist(Set<String> set, File blacklist)
    {
        try
        {
            Scanner scanner = new Scanner(new FileInputStream(blacklist), "UTF-8");

            @SuppressWarnings("serial")
            Type type = new TypeToken<List<String>>()
            {}.getType();
            List<String> data = GSON.fromJson(scanner.useDelimiter("\\A").next(), type);

            set.addAll(data);
            scanner.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}