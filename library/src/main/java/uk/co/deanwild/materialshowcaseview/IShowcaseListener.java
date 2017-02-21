package uk.co.deanwild.materialshowcaseview;


public interface IShowcaseListener {
    void onShowcaseInited(MaterialShowcaseView showcaseView);
    void onShowcaseDisplayed(MaterialShowcaseView showcaseView);
    void onShowcaseDismissed(MaterialShowcaseView showcaseView);
}
