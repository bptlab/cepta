declare module "*.vue" {
  import Vue from "vue";
  export default Vue;
}

declare module "*.gql" {
  import { DocumentNode, DocumentNode } from "graphql";

  const content: DocumentNode;
  export default content;
}

declare module "*.graphql" {
  const content: DocumentNode;
  export default content;
}
