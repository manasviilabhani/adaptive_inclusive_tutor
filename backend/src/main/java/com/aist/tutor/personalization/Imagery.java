package com.aist.tutor.personalization;

import com.aist.tutor.domain.Chapter;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Everyday "picture it" images that make an idea concrete. Each image is matched by keywords in the idea,
 * the chapter name or the student's message. They are deliberately literal and concrete (real objects,
 * real actions) so they also suit learners who find figurative language confusing.
 */
public final class Imagery {

    /**
     * @param text  the picture, in words
     * @param scene id of the animated scene that shows it (null = the generic animation built from the lesson)
     */
    public record Match(String text, String scene) {}

    private record Picture(Pattern keywords, String text, String scene) {}

    private static Picture p(String regex, String text) {
        return p(null, regex, text);
    }

    private static Picture p(String scene, String regex, String text) {
        return new Picture(Pattern.compile("\\b(" + regex + ")(s|es)?\\b", Pattern.CASE_INSENSITIVE), text, scene);
    }

    // Order matters: more specific images come before broader ones.
    private static final List<Picture> PICTURES = List.of(
            // Mathematics
            p("place-value", "place value|digit", "Imagine a street of little houses: the Ones house on the right, then the Tens house, "
                    + "then the Hundreds house, then the Thousands house. A digit is worth more when it lives in a house "
                    + "further to the left. The 5 in the Hundreds house is really worth 500."),
            p("fraction", "fraction|numerator|denominator", "Picture a pizza cut into equal slices. The bottom number tells you how many "
                    + "equal slices the pizza was cut into. The top number tells you how many slices you take. "
                    + "3/8 means: cut into 8 slices, you take 3."),
            p("decimal", "Think of money. 1 rupee is one whole. 50 paise is half of a rupee, which we write as 0.5. "
                    + "The digits after the point are pieces of one whole."),
            p("percent\\w*", "Imagine a grid of 100 small boxes. If you colour 25 of them, you have coloured 25 percent. "
                    + "Percent just means \"out of 100\"."),
            p("integer", "integer|negative", "Imagine a lift in a tall building. Floor 0 is the ground floor. Going up is positive. "
                    + "Going down to the basements is negative: B1 is -1, B2 is -2. Start on floor 7 and go down 3 floors: you are on floor 4."),
            p("ratio|proportion", "Think of making lemonade: 1 cup of juice for every 3 cups of water. That is the ratio 1 : 3. "
                    + "Make a bigger jug with 2 cups of juice, and you need 6 cups of water so it tastes the same."),
            p("equation", "equation|solve for|balance", "Picture a balance scale that is perfectly level. An equation is that level scale. "
                    + "Whatever you add or take away on one side, do the same on the other side, and it stays level."),
            p("equation", "variable|algebra|unknown", "Imagine a closed box with a label \"x\" on it. A number is hidden inside. "
                    + "Algebra is the detective work to find out which number is in the box."),
            p("area-perimeter", "perimeter|boundary", "Imagine an ant walking all the way around the edge of a garden and back to where it started. "
                    + "The distance the ant walks is the perimeter."),
            p("area-perimeter", "area", "Imagine covering a floor with square tiles, with no gaps. The number of tiles you need is the area."),
            p("volume|capacity", "Imagine filling a box with small sugar cubes until it is completely full. "
                    + "The number of cubes that fit inside is its volume."),
            p("angle", "angle|degree", "Picture the two hands of a clock. When they open wider, the angle gets bigger. "
                    + "The corner of a book is a right angle: exactly 90 degrees."),
            p("triangle", "Hold three straws and join their ends to make a closed shape. That shape is a triangle. "
                    + "However you join them, the three corners always add up to 180 degrees, like a straight line."),
            p("circle|radius|diameter", "Imagine tying a string to a pin and walking around it while holding the string tight. "
                    + "Your path is a circle. The string is the radius. Two strings in a straight line across is the diameter."),
            p("probability|chance|likely", "Imagine a bag with 3 red marbles and 1 blue marble. Reach in without looking. "
                    + "Red is more likely: 3 chances out of 4."),
            p("average|arithmetic mean|mean value", "Imagine friends pouring all their sweets into one pile and then sharing them out equally. "
                    + "The number each friend gets is the mean (average)."),
            p("graph|chart|data", "Imagine stacking blocks to show how many students like each fruit. "
                    + "The taller the stack, the more students. That stack picture is a bar graph."),
            p("factor", "multiple|factor|prime", "Imagine arranging 12 chairs in neat rows: 1 row of 12, 2 rows of 6, 3 rows of 4. "
                    + "The row sizes (1, 2, 3, 4, 6, 12) are the factors of 12. A prime number can only make 1 long row."),
            // Science
            p("photosynthesis", "photosynthesis|chlorophyll", "Imagine every leaf is a tiny kitchen. Sunlight is the stove. Water and carbon dioxide "
                    + "are the ingredients. The leaf cooks food (sugar) and lets oxygen float out into the air."),
            p("cell|nucleus|mitochondria|membrane", "Imagine a cell as a tiny city. The nucleus is the city office that keeps the plans (DNA). "
                    + "The mitochondria are power stations. The cell membrane is the city wall with gates that let things in and out."),
            p("atom|electron|proton|neutron", "Picture a tiny solar system. Protons and neutrons sit together in the middle (the nucleus), "
                    + "and electrons move around it in layers called shells."),
            p("circuit", "current|circuit|electric\\w*|battery|batteries", "Imagine water flowing through pipes. The battery is the pump, the wire is the pipe, "
                    + "and the current is the water flowing. Open the switch and it is like a break in the pipe: the flow stops."),
            p("magnet", "Imagine two magnets as two people with the same name tag: same poles push each other away. "
                    + "Opposite poles pull together and click."),
            p("friction", "Imagine sliding in socks on a smooth floor, then on a carpet. The carpet grips your socks more. "
                    + "That grip that slows you down is friction."),
            p("gravity|weight", "Throw a ball up into the air. An invisible pull from the Earth always brings it back down. "
                    + "That pull is gravity."),
            p("force|push|pull|newton", "Imagine pushing a shopping trolley. Your push is a force. Push harder and it speeds up faster. "
                    + "Push it from the front and it slows down."),
            p("speed|velocity|motion", "Imagine two friends running a race. In the same time, the one who covers more distance "
                    + "is faster. Speed is distance divided by time."),
            p("energy", "Imagine a wind-up toy car. Winding it stores energy. When you let go, that stored energy turns into movement."),
            p("reflection|mirror|light", "Imagine throwing a ball at a wall: it bounces back. Light bounces off a mirror in the same way."),
            p("sound|vibrat\\w*", "Drop a stone in a still pond and watch the ripples spread. Sound spreads through air as tiny vibrations, "
                    + "like those ripples."),
            p("water-cycle", "evaporat\\w*|water cycle|condens\\w*", "Imagine a puddle on a sunny day getting smaller and smaller. The water turns into invisible "
                    + "vapour and rises. High up it cools, forms clouds, and falls again as rain."),
            p("digest\\w*|stomach|intestine", "Imagine food going down a long slide through a factory: chewed in the mouth, churned in the stomach, "
                    + "and the useful parts taken into the blood in the small intestine."),
            p("heart|blood|circulat\\w*", "Imagine the heart as a pump in the middle of a delivery network. Blood vessels are the roads, "
                    + "and blood carries oxygen and food to every part of the body."),
            p("food-chain", "food chain|ecosystem|producer|consumer", "Picture a line of who-eats-whom: grass → grasshopper → frog → snake. "
                    + "Energy is passed along the line like a baton in a relay race."),
            p("acid|base|alkali|neutrali\\w*", "Think of lemon juice (sour: an acid) and soap (slippery: a base). "
                    + "Mix an acid with a base and they cancel each other out. That is neutralisation."),
            p("chemical change|chemical reaction|reaction", "Imagine baking a cake. Once the flour, eggs and sugar are baked, you cannot get them back. "
                    + "Something new has formed. That is what happens in a chemical reaction."),
            p("matter", "matter|solid|liquid|gas", "Imagine people in a room. In a solid they stand still, packed close. In a liquid they move around "
                    + "but stay close. In a gas they run all over the room, far apart."),
            p("gene|dna|heredit\\w*|inherit\\w*", "Imagine a recipe book passed from parents to children. Genes are the recipes "
                    + "that decide things like eye colour."),
            // Social Science
            p("vote", "democracy|election|vote", "Imagine your class choosing a class monitor. Everyone gets one vote, and the person with "
                    + "the most votes wins. A democracy works the same way for a whole country."),
            p("constitution|rights|law", "Imagine the rulebook for a big game that everyone agrees to follow. "
                    + "A constitution is the rulebook for a country."),
            p("latitude|longitude|map|globe", "Imagine a giant net of lines wrapped around a ball. Lines going across are latitudes, "
                    + "lines going top to bottom are longitudes. Where two lines cross is an exact address on Earth."),
            p("government|parliament", "Imagine a school council: some students make the rules, some carry them out, and "
                    + "teachers decide if a rule was broken. A government is split into parts in the same way."),
            p("trade|market|economy", "Imagine a weekly village market. Farmers bring vegetables, potters bring pots, and people swap "
                    + "and buy what they need. That is trade."),
            // Economics
            p("demand|supply|price", "Imagine a mango stall. When mangoes are cheap, many people want them (high demand). "
                    + "When few mangoes are left (low supply), the seller can ask a higher price."),
            // English
            p("noun", "Imagine putting name tags on everything in a room: \"chair\", \"Riya\", \"window\". Those names are nouns."),
            p("verb", "Imagine a video of someone: they run, jump, eat, laugh. The doing words are verbs."),
            p("adjective", "Imagine painting a plain picture: a big, red, shiny ball. The words that add the paint are adjectives."),
            p("tense|past tense|future tense", "Imagine a timeline on the floor: yesterday behind you, today under your feet, tomorrow in front. "
                    + "Tense tells you where on the line the action happens."),
            p("poem|poetry|rhyme", "Imagine a song without music. A poem uses rhythm, rhyme and pictures in words to make you feel something."),
            p("story|character|plot", "Imagine a film in your head: the characters are the actors, and the plot is what happens to them, "
                    + "from the beginning to the end."),
            // Computer Science
            p("algorithm", "Think of a recipe for making tea: exact steps, in order. An algorithm is a recipe for a computer."),
            p("loop|iteration", "Imagine running laps around a track until the coach blows the whistle. A loop repeats steps until a condition says stop."),
            p("program|code|python", "Imagine giving very exact instructions to a robot that does exactly what you say, nothing more. That is programming.")
    );

    private Imagery() {
    }

    /** The best picture for this text, or one built from the chapter's own visual example. Never null. */
    public static String picture(Chapter chapter, String... texts) {
        return match(chapter, texts).text();
    }

    /** The best picture and its animated scene, falling back to the chapter's own visual example. */
    public static Match match(Chapter chapter, String... texts) {
        for (String text : texts) {
            if (text == null) continue;
            // The picture whose keyword appears earliest in the text is the one the text is mainly about.
            Picture best = null;
            int bestAt = Integer.MAX_VALUE;
            for (Picture pic : PICTURES) {
                Matcher m = pic.keywords().matcher(text);
                if (m.find() && m.start() < bestAt) {
                    best = pic;
                    bestAt = m.start();
                }
            }
            if (best != null) return new Match(best.text(), best.scene());
        }
        for (Picture pic : PICTURES) {
            if (pic.keywords().matcher(chapter.getChapterName()).find()) return new Match(pic.text(), pic.scene());
        }
        for (Map<String, String> ex : chapter.getExamples()) {
            String visual = ex.getOrDefault("visual", "");
            if (!visual.isBlank()) {
                return new Match("Picture this in your mind: " + visual + "\n(" + ex.get("content") + ")", null);
            }
        }
        if (!chapter.getExamples().isEmpty()) {
            return new Match("Imagine you are watching this happen in front of you: " + chapter.getExamples().get(0).get("content"), null);
        }
        return new Match("Imagine explaining \"" + chapter.getChapterName().toLowerCase(Locale.ROOT)
                + "\" to a friend using things you can see around you.", null);
    }
}
