/**
 * This module extends the out of the box original to include metadata fields
 * that we might want to send to the repository webscript for processing.
 * 
 * @module openesdh/common/services/UploadService.js
 * @extends module:alfresco/services/UploadService
 * @author Lanre Abiwon
 */
define(["dojo/_base/declare","service/constants/Default",
        "alfresco/services/UploadService"],
        function(declare, AlfConstants, UploadService) {
   
   return declare([UploadService], {

      /**
       * Constructs the upload payload object to be added to the fileStore object for each file. 
       * The object constructed is designed to work with the Alfresco REST service for uploading
       * documents. This function can be overridden to support different APIs
       *
       * @instance
       * @param {object} file The file being uploaded
       * @param {object} fileName The name of the file being uploaded
       * @param {object} targetData
       */
      constructUploadData: function alfresco_services_UploadService__constructUploadData(file, fileName, targetData) {
         // TODO: NEED TO UPDATE THIS OBJECT AND INCLUDE DEFAULTS, ETC AS INSTANCE VARIABLES...
         // The object should take the values passed in the upload request rather than having statically
         // defined data created when the widget is instantiated (e.g. this should be able to respond to
         // to allow uploads to varying locations)...
         var uploadData =
         {
            filedata: file,
            filename: fileName,
            destination: targetData.destination,
            siteId: targetData.siteId,
            containerId: targetData.containerId,
            uploaddirectory: targetData.uploadDirectory,
            majorVersion: targetData.majorVersion ? targetData.majorVersion : "true",
            updateNodeRef: targetData.updateNodeRef,
            description: targetData.description,
            overwrite: targetData.overwrite,
            thumbnails: targetData.thumbnails,
            username: targetData.username,
            doccategory : targetData.doc_category ? targetData.doc_category : null,
            docstate : targetData.doc_state ? targetData.doc_state : null,
            doctype : targetData.doc_type ? targetData.doc_type : null
         };
         return uploadData;
      },

      /**
       * Starts the actual upload for a file
       *
       * @instance
       * @param {object} Contains info about the file and its request.
       */
      startFileUpload: function alfresco_services_UploadService__startFileUpload(fileInfo)
      {
         // Mark file as being uploaded
         fileInfo.state = this.STATE_UPLOADING;

         var url;
         if (this.uploadURL === null || this.uploadURL === undefined)
         {
            url = AlfConstants.PROXY_URI + "api/upload";
         }
         else
         {
            url = AlfConstants.PROXY_URI + this.uploadURL;
         }
         if (this.isCsrfFilterEnabled())
         {
            url += "?" + this.getCsrfParameter() + "=" + encodeURIComponent(this.getCsrfToken());
         }

         if (this.uploadMethod === this.FORMDATA_UPLOAD)
         {
            // TODO: This should be able to respond to variable uploadData (to support overridden constructUploadData functions)...
            var formData = new FormData();
            formData.append("filedata", fileInfo.uploadData.filedata);
            formData.append("filename", fileInfo.uploadData.filename);
            formData.append("destination", fileInfo.uploadData.destination);
            formData.append("siteId", fileInfo.uploadData.siteId);
            formData.append("containerId", fileInfo.uploadData.containerId);
            formData.append("uploaddirectory", fileInfo.uploadData.uploaddirectory);
            formData.append("majorVersion", fileInfo.uploadData.majorVersion ? fileInfo.uploadData.majorVersion : "false");
            formData.append("username", fileInfo.uploadData.username);
            formData.append("overwrite", fileInfo.uploadData.overwrite);
            formData.append("thumbnails", fileInfo.uploadData.thumbnails);
            formData.append("doc_category", fileInfo.uploadData.doccategory);
            formData.append("doc_state", fileInfo.uploadData.docstate);
            formData.append("doc_type", fileInfo.uploadData.doctype);

            if (fileInfo.uploadData.updateNodeRef)
            {
               formData.append("updateNodeRef", fileInfo.uploadData.updateNodeRef);
            }
            if (fileInfo.uploadData.description)
            {
               formData.append("description", fileInfo.uploadData.description);
            }
            fileInfo.request.open("POST",  url, true);
            fileInfo.request.send(formData);
         }
      }
   });
});