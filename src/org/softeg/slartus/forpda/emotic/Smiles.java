package org.softeg.slartus.forpda.emotic;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: slinkin
 * Date: 17.10.12
 * Time: 9:27
 * To change this template use File | Settings | File Templates.
 */
public class Smiles extends ArrayList<Smile> {

    public Smiles(){
        fill();
    }
    
    public String[] getFilesList(){
        String[] res=new String[size()];
        String path="forum/style_emoticons/default/";
        for (int i=0;i<size();i++)
            res[i]=path+this.get(i).FileName;

        return res;
    }
    
    public void CreatePanel(){

    }
    
    private void fill(){
        add(new Smile(":happy:","happy.gif"));
        add(new Smile(";)","wink.gif"));
        add(new Smile(":P","tongue.gif"));
        add(new Smile(":lol:","laugh.gif"));
        add(new Smile(":mellow:","mellow.gif"));
        add(new Smile(":huh:","huh.gif"));
        add(new Smile(":o","ohmy.gif"));
        add(new Smile(":-D","biggrin.gif"));
        add(new Smile("B)","cool.gif"));
        add(new Smile(":rolleyes:","rolleyes.gif"));
        add(new Smile("-_-","sleep.gif"));
        add(new Smile("&lt;_&lt;","dry.gif"));
        add(new Smile(":)","smile.gif"));
        add(new Smile(":wub:","wub.gif"));
        add(new Smile(":angry:","angry.gif"));
        add(new Smile(":(","sad.gif"));
        add(new Smile(":unsure:","unsure.gif"));
        add(new Smile(":wacko:","wacko.gif"));
        add(new Smile(":blink:","blink.gif"));
        add(new Smile(":ph34r:","ph34r.gif"));
        add(new Smile(":banned:","banned.gif"));
        add(new Smile(":antifeminism:","antifeminism.gif"));
        add(new Smile(":beee:","beee.gif"));
        add(new Smile(":beta:","beta.gif"));
        add(new Smile(":boy_girl:","boy_girl.gif"));
        add(new Smile(":butcher:","butcher.gif"));
        add(new Smile(":bubble:","bubble.gif"));
        add(new Smile(":censored:","censored.gif"));
        add(new Smile(":clap:","clap.gif"));
        add(new Smile(":close_tema:","close_tema.gif"));
        add(new Smile(":clapping:","clapping.gif"));
        add(new Smile(":coldly:","coldly.gif"));
        add(new Smile(":comando:","comando.gif"));
        add(new Smile(":congratulate:","congratulate.gif"));
        add(new Smile(":dance:","dance.gif"));
        add(new Smile(":daisy:","daisy.gif"));
        add(new Smile(":dancer:","dancer.gif"));
        add(new Smile(":derisive:","derisive.gif"));
        add(new Smile(":dinamo:","dinamo.gif"));
        add(new Smile(":dirol:","dirol.gif"));
        add(new Smile(":diver:","diver.gif"));
        add(new Smile(":drag:","drag.gif"));
        add(new Smile(":download:","download.gif"));
        add(new Smile(":drinks:","drinks.gif"));
        add(new Smile(":first_move:","first_move.gif"));
        add(new Smile(":feminist:","feminist.gif"));
        add(new Smile(":flood:","flood.gif"));
        add(new Smile(":fool:","fool.gif"));
        add(new Smile(":friends:","friends.gif"));
        add(new Smile(":foto:","foto.gif"));
        add(new Smile(":girl_blum:","girl_blum.gif"));
        add(new Smile(":girl_crazy:","girl_crazy.gif"));
        add(new Smile(":girl_curtsey:","girl_curtsey.gif"));
        add(new Smile(":girl_dance:","girl_dance.gif"));
        add(new Smile(":girl_flirt:","girl_flirt.gif"));
        add(new Smile(":girl_hospital:","girl_hospital.gif"));
        add(new Smile(":girl_hysterics:","girl_hysterics.gif"));
        add(new Smile(":girl_in_love:","girl_in_love.gif"));
        add(new Smile(":girl_kiss:","girl_kiss.gif"));
        add(new Smile(":girl_pinkglassesf:","girl_pinkglassesf.gif"));
        add(new Smile(":girl_parting:","girl_parting.gif"));
        add(new Smile(":girl_prepare_fish:","girl_prepare_fish.gif"));
        add(new Smile(":good:","good.gif"));
        add(new Smile(":girl_spruce_up:","girl_spruce_up.gif"));
        add(new Smile(":girl_tear:","girl_tear.gif"));
        add(new Smile(":girl_tender:","girl_tender.gif"));
        add(new Smile(":girl_teddy:","girl_teddy.gif"));
        add(new Smile(":girl_to_babruysk:","girl_to_babruysk.gif"));
        add(new Smile(":girl_to_take_umbrage:","girl_to_take_umbrage.gif"));
        add(new Smile(":girl_triniti:","girl_triniti.gif"));
        add(new Smile(":girl_tongue:","girl_tongue.gif"));
        add(new Smile(":girl_wacko:","girl_wacko.gif"));
        add(new Smile(":girl_werewolf:","girl_werewolf.gif"));
        add(new Smile(":girl_witch:","girl_witch.gif"));
        add(new Smile(":grabli:","grabli.gif"));
        add(new Smile(":good_luck:","good_luck.gif"));
        add(new Smile(":guess:","guess.gif"));
        add(new Smile(":hang:","hang.gif"));
        add(new Smile(":heart:","heart.gif"));
        add(new Smile(":help:","help.gif"));
        add(new Smile(":helpsmilie:","helpsmilie.gif"));
        add(new Smile(":hemp:","hemp.gif"));
        add(new Smile(":heppy_dancing:","heppy_dancing.gif"));
        add(new Smile(":hysterics:","hysterics.gif"));
        add(new Smile(":indeec:","indeec.gif"));
        add(new Smile(":i-m_so_happy:","i-m_so_happy.gif"));
        add(new Smile(":kindness:","kindness.gif"));
        add(new Smile(":king:","king.gif"));
        add(new Smile(":laugh_wild:","laugh_wild.gif"));
        add(new Smile(":4PDA:","love_4PDA.gif"));
        add(new Smile(":nea:","nea.gif"));
        add(new Smile(":moil:","moil.gif"));
        add(new Smile(":no:","no.gif"));
        add(new Smile(":nono:","nono.gif"));
        add(new Smile(":offtopic:","offtopic.gif"));
        add(new Smile(":ok:","ok.gif"));
        add(new Smile(":papuas:","papuas.gif"));
        add(new Smile(":party:","party.gif"));
        add(new Smile(":pioneer_smoke:","pioneer_smoke.gif"));
        add(new Smile(":pipiska:","pipiska.gif"));
        add(new Smile(":protest:","protest.gif"));
        add(new Smile(":popcorm:","popcorm.gif"));
        add(new Smile(":rabbi:","rabbi.gif"));
        add(new Smile(":resent:","resent.gif"));
        add(new Smile(":roll:","roll.gif"));
        add(new Smile(":rofl:","rofl.gif"));
        add(new Smile(":rtfm:","rtfm.gif"));
        add(new Smile(":russian_garmoshka:","russian_garmoshka.gif"));
        add(new Smile(":russian:","russian.gif"));
        add(new Smile(":russian_ru:","russian_ru.gif"));
        add(new Smile(":scratch_one-s_head:","scratch_one-s_head.gif"));
        add(new Smile(":scare:","scare.gif"));
        add(new Smile(":search:","search.gif"));
        add(new Smile(":secret:","secret.gif"));
        add(new Smile(":skull:","skull.gif"));
        add(new Smile(":shok:","shok.gif"));
        add(new Smile(":sorry:","sorry.gif"));
        add(new Smile(":smoke:","smoke.gif"));
        add(new Smile(":spiteful:","spiteful.gif"));
        add(new Smile(":stop_flood:","stop_flood.gif"));
        add(new Smile(":suicide:","suicide.gif"));
        add(new Smile(":stop_holywar:","stop_holywar.gif"));
        add(new Smile(":superman:","superman.gif"));
        add(new Smile(":superstition:","superstition.gif"));
        add(new Smile(":sveta:","sveta.gif"));
        add(new Smile(":tablet_za:","tablet_protiv.gif"));
        add(new Smile(":tablet_protiv:","tablet_za.gif"));
        add(new Smile(":thank_you:","thank_you.gif"));
        add(new Smile(":this:","this.gif"));
        add(new Smile(":tomato:","tomato.gif"));
        add(new Smile(":to_clue:","to_clue.gif"));
        add(new Smile(":tommy:","tommy.gif"));
        add(new Smile(":tongue3:","tongue3.gif"));
        add(new Smile(":umnik:","umnik.gif"));
        add(new Smile(":victory:","victory.gif"));
        add(new Smile(":vinsent:","vinsent.gif"));
        add(new Smile(":wallbash:","wallbash.gif"));
        add(new Smile(":whistle:","whistle.gif"));
        add(new Smile(":wink_kind:","wink_kind.gif"));
        add(new Smile(":yahoo:","yahoo.gif"));
        add(new Smile(":yes:","yes.gif"));
        add(new Smile(":[","confusion.gif"));
        add(new Smile("]-:{","girl_devil.gif"));
        add(new Smile(":*","kiss.gif"));
        add(new Smile(":)","smile_good.gif"));
        add(new Smile("@}-&#39;-,-","give_rose.gif"));
        add(new Smile(":&#39;(","cry.gif"));
        add(new Smile("}-)","devil.gif"));
        add(new Smile(":-{","mad.gif"));
        add(new Smile("=^.^=","kitten.gif"));
        add(new Smile(":girl_cray:","girl_cray.gif"));
        add(new Smile("(-=","girl_hide.gif"));
        add(new Smile("(-;","girl_wink.gif"));
        add(new Smile(")-:{","girl_angry.gif"));
        add(new Smile("*-:","girl_chmok.gif"));
        add(new Smile(")-:","girl_sad.gif"));
        add(new Smile(":girl_mad:","girl_mad.gif"));
        add(new Smile("(-:","girl_smile.gif"));
        add(new Smile(":acute:","acute.gif"));
        add(new Smile(":aggressive:","aggressive.gif"));
        add(new Smile(":air_kiss:","air_kiss.gif"));
        add(new Smile(":D","biggrin2.gif"));
        add(new Smile("o.O","blink2.gif"));
        add(new Smile("o_O","blink3.gif"));
        add(new Smile("o_O","blink4.gif"));
        add(new Smile("o.O","blink5.gif"));
        add(new Smile("о_О","blink6.gif"));
        add(new Smile("о_О","blink7.gif"));
        add(new Smile("о.О","blink8.gif"));
        add(new Smile("о.О","blink9.gif"));
        add(new Smile(":blush:","blush.gif"));
        add(new Smile(":yes2:","yes2.gif"));
        add(new Smile(":blush:","blush.gif"));
        add(new Smile(":yes2:","yes2.gif"));
        add(new Smile("В)","cool2.gif"));
        add(new Smile(":-[","confusion2.gif"));
        add(new Smile("В-)","cool3.gif"));
        add(new Smile(":&#39;-(","cry2.gif"));
        add(new Smile(":lol_girl:","girl_haha.gif"));
        add(new Smile(")-&#39;:","girl_cray2.gif"));
        add(new Smile(":girl_cray:","girl_cray.gif"));
        add(new Smile(":girl_cray:","girl_cray.gif"));
        add(new Smile("(;","girl_wink2.gif"));
        add(new Smile(":-*","kiss2.gif"));
        add(new Smile(":laugh:","laugh2.gif"));
        add(new Smile(":ohmy:","ohmy2.gif"));
        add(new Smile(":-(","sad2.gif"));
        add(new Smile("8-)","rolleyes2.gif"));
        add(new Smile(":-)","smile_good2.gif"));
        add(new Smile(":smile:","smile2.gif"));
        add(new Smile(":-P","tongue4.gif"));
        add(new Smile(":-P","tongue2.gif"));
        add(new Smile(":-р","tongue5.gif"));
        add(new Smile(":-р","tongue6.gif"));
        add(new Smile(";-)","wink2.gif"));
        add(new Smile(":girl_cray:","girl_cray.gif"));
    }

}
