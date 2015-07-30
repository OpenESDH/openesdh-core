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
     * @param note
     *            Note data object
     * @return node ref of the newly created note
     */
    public NodeRef createNote(Note note);

    /**
     * Updates note info
     * 
     * @param note
     *            Note data object
     */
    public void updateNote(Note note);

    /**
     * Retrieves all notes of the provided parent node
     * 
     * @param parentNodeRef
     *            Parent node to retrieve the notes for
     * @return a list of notes for the provide parent node
     */
    public List<Note> getNotes(NodeRef parentNodeRef);

    /**
     * Deletes note
     * 
     * @param noteRef
     *            node ref of the note to delete
     */
    public void deleteNote(NodeRef noteRef);
}
