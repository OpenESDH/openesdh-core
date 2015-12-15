package dk.openesdh.repo.services.authorities;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import dk.openesdh.repo.helper.CaseHelper;
import dk.openesdh.repo.services.authorities.GroupsCsvParser.Group;

public class GroupsCsvParserTest {
    
    private static String CSV_HEADER = "Group name,Display name,Member of groups,Simple case,Staff case\n";

    @Test
    public void shouldParseGroups() throws IOException{
        String csv = new StringBuilder(CSV_HEADER)
            .append("IT,IT group,,Read\n")
            .append("HR,HR group,,,Read\n")
            .append("HR_CREATE,HR creators,HR,,CREATE;WRITE\n")
            .append("HR_CHIEF,HR chief,HR;HR_CREATE").toString();
        GroupsCsvParser parser = new GroupsCsvParser(Arrays.asList("simple", "staff"));
        List<Group> groups = parser.parse(IOUtils.toInputStream(csv));
        Assert.assertFalse("The result groups list shoudn't be empty", groups.isEmpty());

        Group itGroup = groups.get(0);
        Assert.assertEquals("Wrong first group name", "IT", itGroup.getShortName());
        Assert.assertTrue("IT group should be able to read simple cases",
                itGroup.getMemberOfGroups().contains(CaseHelper.CASE_READER_ROLE));

        Group hrGroup = groups.get(1);
        Assert.assertEquals("Wrong second group name", "HR", hrGroup.getShortName());
        Assert.assertTrue("HR group should be able to read staff cases",
                hrGroup.getMemberOfGroups().contains("CaseStaffReader"));

        Group hrCreatorsGroup = groups.get(2);
        Assert.assertEquals("Wrong HR creators group name", "HR_CREATE", hrCreatorsGroup.getShortName());
        Assert.assertTrue("HR creators should be a member of HR group", 
                hrCreatorsGroup.getMemberOfGroups().contains("HR"));
        Assert.assertTrue("HR creators should be able to create staff cases", 
                hrCreatorsGroup.getMemberOfGroups().contains("CaseStaffCreator"));
        Assert.assertTrue("HR creators should be able to write in staff cases", 
                hrCreatorsGroup.getMemberOfGroups().contains("CaseStaffWriter"));

        Group hrChief = groups.get(3);
        Assert.assertTrue("HR chief group should be a member of HR group",
                hrChief.getMemberOfGroups().contains("HR"));
        Assert.assertTrue("HR chief group should be a member of HR creators group",
                hrChief.getMemberOfGroups().contains("HR_CREATE"));
    }

    @Test
    public void shouldThrowWrongCaseTypeInCsvHeader() throws IOException {
        String csvBadHeader = "Group name,Display name,Member of groups,NonExistType case";
        GroupsCsvParser parser = new GroupsCsvParser(Arrays.asList("simple", "staff"));
        try {
            parser.parse(IOUtils.toInputStream(csvBadHeader));
            Assert.fail("Should throw exception wrong case type in csv header");
        } catch (Exception e) {
            Assert.assertTrue("Wrong exception message. Should contain \"Invalid case type provided\"", 
                    e.getMessage().contains("Invalid case type provided"));
        }
    }
}
