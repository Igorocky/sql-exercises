package org.igye.sqlexercises.data;

import org.apache.commons.lang3.StringUtils;
import org.igye.sqlexercises.common.TextToken;
import org.igye.sqlexercises.data.repository.EngTextRepository;
import org.igye.sqlexercises.data.repository.ParagraphRepository;
import org.igye.sqlexercises.data.repository.WordRepository;
import org.igye.sqlexercises.exceptions.OutlineException;
import org.igye.sqlexercises.htmlforms.ChangeAttrValueRequest;
import org.igye.sqlexercises.htmlforms.CreateEngTextForm;
import org.igye.sqlexercises.htmlforms.CreateWordRequest;
import org.igye.sqlexercises.htmlforms.DeleteWordRequest;
import org.igye.sqlexercises.htmlforms.EngTextDto;
import org.igye.sqlexercises.htmlforms.IgnoreWordRequest;
import org.igye.sqlexercises.htmlforms.SessionData;
import org.igye.sqlexercises.htmlforms.WordDto;
import org.igye.sqlexercises.model.EngText;
import org.igye.sqlexercises.model.TextLanguage;
import org.igye.sqlexercises.model.User;
import org.igye.sqlexercises.model.Word;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.igye.sqlexercises.common.OutlineUtils.createResponse;
import static org.igye.sqlexercises.common.OutlineUtils.createVoidResponse;
import static org.igye.sqlexercises.common.OutlineUtils.filter;
import static org.igye.sqlexercises.common.OutlineUtils.map;
import static org.igye.sqlexercises.common.TextProcessing.ALL_GROUPS;
import static org.igye.sqlexercises.common.TextProcessing.ALL_WORDS;
import static org.igye.sqlexercises.common.TextProcessing.splitOnSentences;

@Component
public class WordsDao {
    @Autowired
    private SessionData sessionData;
    @Autowired
    private ParagraphRepository paragraphRepository;
    @Autowired
    private EngTextRepository engTextRepository;
    @Autowired
    private WordRepository wordRepository;
    @Autowired
    private NodeDao nodeDao;

    @Transactional
    public EngText createEngText(UUID parentId, CreateEngTextForm form) {
        User currUser = sessionData.getCurrentUser();
        EngText engText = new EngText();
        engText.setName(form.getName());
        engText.setOwner(currUser);
        engText.setText("");
        engText.setIgnoreList("");
        engText.setLearnGroups("");
        engText.setLanguage(TextLanguage.EN);
        nodeDao.saveNode(parentId, engText, node -> engTextRepository.save((EngText) node));
        return engText;
    }

    @Transactional
    public EngTextDto getEngTextDtoById(UUID id) {
        EngText text = getEngTextById(id);

        EngTextDto res = EngTextDto.builder()
                .textId(text.getId())
                .title(text.getName())
                .text(text.getText())
                .sentences(splitOnSentences(text))
                .ignoreList(text.getIgnoreList())
                .wordsToLearn(map(text.getWords(), this::mapWord))
                .language(text.getLanguage())
                .pct(text.getPct())
                .build();
        Collections.sort(res.getWordsToLearn(), Comparator.comparing(WordDto::getWordInText));
        return res;
    }

    @Transactional
    public WordDto getWordDtoById(UUID wordId) {
        Word word = wordRepository.findByOwnerAndId(sessionData.getCurrentUser(), wordId);
        EngText text = word.getEngText();

        List<List<TextToken>> exampleSentences = splitOnSentences(text)
                .stream()
                .filter(tokens -> tokens.stream()
                        .anyMatch(token -> token.getValue().equals(word.getWordInText()))
                )
                .collect(Collectors.toList());

        WordDto WordDto = mapWord(word);
        WordDto.setExamples(exampleSentences);
        return WordDto;
    }

    @Transactional
    public EngText getEngTextById(UUID id) {
        EngText text = engTextRepository.findByOwnerAndId(sessionData.getCurrentUser(), id);
        if (text == null) {
            throw new OutlineException("Text with id " + id + " was not found.");
        }
        return text;
    }

    @Transactional
    public Word getWordById(UUID id) {
        Word word = wordRepository.findByOwnerAndId(sessionData.getCurrentUser(), id);
        if (word == null) {
            throw new OutlineException("Word with id " + id + " was not found.");
        }
        return word;
    }

    private WordDto mapWord(Word word) {
        return WordDto.builder()
                .id(word.getId())
                .group(word.getGroup())
                .wordInText(word.getWordInText())
                .word(word.getWord())
                .transcription(word.getTranscription())
                .meaning(word.getMeaning())
                .build();
    }

    @Transactional
    public Object changeAttr(ChangeAttrValueRequest request) {
        if ("eng-text-title".equals(request.getAttrName())) {
            getEngTextById(request.getObjId()).setName((String) request.getValue());
            return request.getValue();
        } else if ("eng-text-text".equals(request.getAttrName())) {
            getEngTextById(request.getObjId()).setText((String) request.getValue());
            return request.getValue();
        } else if ("eng-text-lang".equals(request.getAttrName())) {
            getEngTextById(request.getObjId()).setLanguage(TextLanguage.valueOf((String) request.getValue()));
            return request.getValue();
        } else if ("eng-text-pct".equals(request.getAttrName())) {
            getEngTextById(request.getObjId()).setPct(Integer.parseInt((String) request.getValue()));
            return request.getValue();
        } else if ("eng-text-ignore-list".equals(request.getAttrName())) {
            getEngTextById(request.getObjId()).setIgnoreList((String) request.getValue());
            return request.getValue();
        } else if ("eng-text-learn-groups".equals(request.getAttrName())) {
            getEngTextById(request.getObjId()).setLearnGroups((String) request.getValue());
            return request.getValue();
        } else if ("eng-text-word-group".equals(request.getAttrName())) {
            String newGroupName = (String) request.getValue();
            if (!ALL_GROUPS.equals(newGroupName) && !ALL_WORDS.equals(newGroupName)) {
                getWordById(request.getObjId()).setGroup(newGroupName);
                return request.getValue();
            } else {
                throw new OutlineException("Group name cannot be one of " + ALL_WORDS + " and " + ALL_GROUPS);
            }
        } else if ("eng-text-word-wordInText".equals(request.getAttrName())) {
            getWordById(request.getObjId()).setWordInText((String) request.getValue());
            return request.getValue();
        } else if ("eng-text-word-spelling".equals(request.getAttrName())) {
            getWordById(request.getObjId()).setWord((String) request.getValue());
            return request.getValue();
        } else if ("eng-text-word-transcription".equals(request.getAttrName())) {
            getWordById(request.getObjId()).setTranscription((String) request.getValue());
            return request.getValue();
        } else if ("eng-text-word-meaning".equals(request.getAttrName())) {
            getWordById(request.getObjId()).setMeaning((String) request.getValue());
            return request.getValue();
        } else {
            throw new OutlineException("Unrecognized ChangeAttrValueRequest.attrName: " + request.getAttrName());
        }
    }

    @Transactional
    public WordDto createWord(CreateWordRequest request) {
        EngText text = getEngTextById(request.getEngTextId());
        Word word = Word.builder()
                .id(UUID.randomUUID())
                .group(request.getWord().getGroup())
                .wordInText(request.getWord().getWordInText())
                .word(request.getWord().getWord())
                .transcription(request.getWord().getTranscription())
                .meaning(request.getWord().getMeaning())
                .build();
        text.addWord(word);
        return mapWord(word);
    }

    @Transactional
    public void deleteWord(DeleteWordRequest request) {
        getEngTextById(request.getEngTextId()).detachWordById(request.getWordId());
    }

    @Transactional
    public void ignoreWord(IgnoreWordRequest request) {
        EngText text = getEngTextById(request.getEngTextId());
        String[] arr = StringUtils.split(text.getIgnoreList(), '\n');
        List<String> list = new ArrayList<>(Arrays.asList(arr));
        list.add(request.getSpelling().trim());
        text.setIgnoreList(StringUtils.join(list, '\n'));
    }

    @Transactional
    public void unignoreWord(IgnoreWordRequest request) {
        EngText text = getEngTextById(request.getEngTextId());
        String[] arr = StringUtils.split(text.getIgnoreList(), '\n');
        List<String> list = new ArrayList<>(Arrays.asList(arr));
        while (list.remove(request.getSpelling().trim())) {};
        text.setIgnoreList(StringUtils.join(list, '\n'));
    }

    @Transactional
    public void changeLearnGroups(UUID textId, List<String> learnGroups) {
        EngText text = getEngTextById(textId);
        text.setLearnGroups(StringUtils.join(learnGroups, '\n'));
    }

    @Transactional
    public Set<String> listAvailableWordGroups(UUID id) {
        EngText text = getEngTextById(id);
        Set<String> availableGroups = text.getWords().stream()
                .map(Word::getGroup)
                .filter(v -> v != null)
                .collect(Collectors.toSet());
        if (!availableGroups.contains(ALL_WORDS)) {
            availableGroups.add(ALL_WORDS);
        }
        if (!availableGroups.contains(ALL_GROUPS)) {
            availableGroups.add(ALL_GROUPS);
        }
        return availableGroups;
    }

    @Transactional
    public Map<String, Object> getLearnGroupsInfo(UUID textId) {
        EngText text = getEngTextById(textId);
        Set<String> allAvailableGroups = listAvailableWordGroups(textId);
        List<String> selectedGroups = text.getListOfLearnGroups();
        Collections.sort(selectedGroups);
        List<String> availableGroups = new ArrayList<>(allAvailableGroups);
        availableGroups.removeAll(selectedGroups);
        Collections.sort(availableGroups);
        return createResponse(
                "available", availableGroups,
                "selected", selectedGroups
        );
    }

    @Transactional
    public Map<String, Object> getSentenceForLearning(UUID textId, Optional<Integer> sentenceIdxOpt) {
        EngText text = getEngTextById(textId);
        List<List<TextToken>> sentences = splitOnSentences(text);

        Integer sentenceIdx = sentenceIdxOpt.orElse(
                sessionData.getCyclicRandom().getRandomIndex(sentences.hashCode(), sentences.size())
        );

        if (sentenceIdx < 0 || sentences.size() <= sentenceIdx) {
            return createSentenceResponse(
                    sentences,
                    null,
                    null,
                    createTaskDescription(sentenceIdxOpt.isPresent(), text.getListOfLearnGroups()),
                    null,
                    null
            );
        } else {
            List<TextToken> sentence = sentences.get(sentenceIdx);
            int[] countsBefore = null;
            int[] countsAfter = null;
            if (text.getListOfLearnGroups().contains(ALL_WORDS)) {
                List<TextToken> hiddable = filter(sentence, TextToken::isHiddable);
                int hash = sentence.hashCode();
                countsBefore = sessionData.getLearnTextData().getCountsStat(hiddable.size(), hash);
                if (!hiddable.isEmpty()) {
                    sessionData
                            .getLearnTextData()
                            .getIndicesToHide(hiddable.size(), text.getPct(), sentence.hashCode())
                            .forEach(idx ->
                                    hiddable.get(idx).setHidden(true)
                            );
                }
                countsAfter = sessionData.getLearnTextData().getCountsStat(hiddable.size(), hash);
            } else if (text.getListOfLearnGroups().contains(ALL_GROUPS)) {
                sentence.forEach(t -> {
                    if (t.isWordToLearn()) {
                        t.setHidden(true);
                    }
                });
            } else {
                sentence.forEach(t -> {
                    if (t.isSelectedGroup()) {
                        t.setHidden(true);
                    }
                });
            }
            return createSentenceResponse(
                    sentences,
                    sentence,
                    sentenceIdx,
                    createTaskDescription(sentenceIdxOpt.isPresent(), text.getListOfLearnGroups()),
                    countsBefore,
                    countsAfter
            );
        }
    }

    @Transactional
    public Map<String, Object> getWordForLearning(UUID textId) {
        EngText text = getEngTextById(textId);
        List<String> selectedGroupsList = text.getListOfLearnGroups();
        List<Word> wordsInSelectedGroups = null;
        Set<String> selectedGroups = new HashSet<>(selectedGroupsList);
        if (selectedGroups.contains(ALL_GROUPS) || selectedGroups.contains(ALL_WORDS)) {
            wordsInSelectedGroups = text.getWords().stream()
                    .sorted(Comparator.comparing(Word::getId))
                    .collect(Collectors.toList());
        } else {
            wordsInSelectedGroups = text.getWords().stream()
                    .filter(word -> selectedGroups.contains(word.getGroup()))
                    .sorted(Comparator.comparing(Word::getId))
                    .collect(Collectors.toList());
        }
        Word word = wordsInSelectedGroups.get(
                sessionData.getCyclicRandom().getRandomIndex(
                        map(wordsInSelectedGroups, w -> w.getWordInText() + w.getWord()).hashCode(),
                        wordsInSelectedGroups.size()
                )
        );
        WordDto WordDto = getWordDtoById(word.getId());
        return createWordResponse(
                selectedGroupsList,
                sessionData.getCyclicRandom().getCounts(),
                WordDto
        );
    }

    private String createTaskDescription(boolean isSequential, List<String> groups) {
        StringBuilder sb = new StringBuilder();
        if (isSequential) {
            sb.append("Fill gaps in a sentence.");
        } else {
            sb.append("Fill gaps in a sentence: random mode.");
        }
        sb.append(" Groups: ").append(StringUtils.join(groups, ", ")).append(".");
        return sb.toString();
    }

    private Map<String, Object> createSentenceResponse(
            List<List<TextToken>> sentences,
            List<TextToken> sentence,
            Integer sentenceIdx,
            String taskDescription,
            int[] countsBefore,
            int[] countsAfter
    ) {
        Map<String, Object> response = createVoidResponse();
        response.put("maxSentenceIdx", sentences.size() - 1);
        response.put("sentence", sentence);
        response.put("sentenceIdx", sentenceIdx);
        response.put("taskDescription", taskDescription);
        response.put("countsBefore", countsBefore);
        response.put("countsAfter", countsAfter);
        return response;
    }

    private Map<String, Object> createWordResponse(
            List<String> selectedGroups,
            String counts,
            WordDto word
    ) {
        Map<String, Object> res = createVoidResponse();
        res.put("groups", StringUtils.join(selectedGroups, ", "));
        res.put("counts", counts);
        res.put("word", word);
        return res;
    }

    @Transactional
    public WordDto getWordByWordInText(UUID textId, String wordInText) {
        EngText text = getEngTextById(textId);
        return text.getWords().stream()
                .filter(word -> wordInText.equals(word.getWordInText()))
                .findFirst()
                .map(this::mapWord)
                .orElse(null);
    }

    @Transactional
    public void moveWord(UUID selectedId, UUID destId) {
        Word wordToMove = wordRepository.findByOwnerAndId(sessionData.getCurrentUser(), selectedId);
        EngText textToMoveTo = engTextRepository.findByOwnerAndId(sessionData.getCurrentUser(), destId);
        wordToMove.getEngText().detachWordById(wordToMove.getId());
        textToMoveTo.addWord(wordToMove);
    }
}
