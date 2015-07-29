package dk.openesdh.repo.services.notes;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

import dk.openesdh.repo.model.Note;

/**
 * Created by syastrov on 2/6/15.
 */
public interface NoteService {
    /**
     * Creates a note for the provided parent node
     * 
     * @param parentNodeRef
     *            Parent node to create a new note for
     * @param content
     *            note message
     * @param author
     *            note author
     * @return node ref of the newly created note
     */
    public NodeRef createNote(NodeRef parentNodeRef, String content, String author);

    /**
     * Creates a note for the provided parent node
     * 
     * @param note
     *            Note data object
     * @return node ref of the newly created note
     */
    public NodeRef createNote(Note note);

    /**
     * Updates note info
     * 
     * @param noteRef
     *            node ref of the note to update
     * @param content
     *            a new content of the note
     * @param author
     *            a new author of the note
     */
    public void updateNote(NodeRef noteRef, String content, String author);

    /**
     * Retrieves all notes of the provided parent node
     * 
     * @param parentNodeRef
     *            Parent node to retrieve the notes for
     * @return a list of notes for the provide parent node
     */
    public List<NodeRef> getNotes(NodeRef parentNodeRef);

    /**
     * Retrieves all notes of the provided parent node
     * 
     * @param parentNodeRef
     *            Parent node to retrieve the notes for
     * @return a list of notes for the provide parent node
     */
    public List<Note> getObjectNotes(NodeRef parentNodeRef);

    /**
     * Deletes note
     * 
     * @param noteRef
     *            node ref of the note to delete
     */
    public void deleteNote(NodeRef noteRef);
}
