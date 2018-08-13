package sepm.englishgo;

public class Word {
    private String id;
    private String content;
    private int level;
    private int topic;
    private String hint;
    private String explanation;
    private String sampleSen;

    public Word() {
    }

    public Word(String id, String content, int level, int topic, String hint, String explanation, String sampleSen) {
        this.id = id;
        this.content = content;
        this.level = level;
        this.topic = topic;
        this.hint = hint;
        this.explanation = explanation;
        this.sampleSen = sampleSen;
    }

    public String getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public int getLevel() {
        return level;
    }

    public int getTopic() {
        return topic;
    }

    public String getHint() {
        return hint;
    }

    public String getExplanation() {
        return explanation;
    }

    public String getSampleSen() {
        return sampleSen;
    }
}
