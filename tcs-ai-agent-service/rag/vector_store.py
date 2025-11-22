import os
from typing import List, Optional
from langchain.schema import Document
from langchain_chroma import Chroma
from langchain_google_genai import GoogleGenerativeAIEmbeddings
from config import settings


class VectorStoreManager:
    """Manage Chroma vector store for news articles"""

    def __init__(self):
        self.embeddings = GoogleGenerativeAIEmbeddings(
            model=settings.embedding_model,
            google_api_key=settings.gemini_api_key
        )
        self.persist_directory = settings.chroma_persist_directory
        self.collection_name = settings.chroma_collection_name
        self._vector_store: Optional[Chroma] = None

    def get_vector_store(self) -> Chroma:
        """Get or create the vector store"""
        if self._vector_store is None:
            # Create directory if it doesn't exist
            os.makedirs(self.persist_directory, exist_ok=True)

            self._vector_store = Chroma(
                collection_name=self.collection_name,
                embedding_function=self.embeddings,
                persist_directory=self.persist_directory,
            )

        return self._vector_store

    def add_documents(self, documents: List[Document]) -> List[str]:
        """
        Add documents to the vector store

        Args:
            documents: List of Document objects

        Returns:
            List of document IDs
        """
        if not documents:
            return []

        vector_store = self.get_vector_store()

        # Filter out duplicates based on URL in metadata
        unique_docs = self._filter_duplicates(documents)

        if not unique_docs:
            print("No new documents to add")
            return []

        # Add documents to vector store
        ids = vector_store.add_documents(unique_docs)
        print(f"Added {len(ids)} new documents to vector store")

        return ids

    def similarity_search(
        self, query: str, k: int = 5, filter_dict: Optional[dict] = None
    ) -> List[Document]:
        """
        Search for similar documents

        Args:
            query: Search query
            k: Number of results to return
            filter_dict: Optional metadata filter

        Returns:
            List of similar documents
        """
        vector_store = self.get_vector_store()

        if filter_dict:
            results = vector_store.similarity_search(query, k=k, filter=filter_dict)
        else:
            results = vector_store.similarity_search(query, k=k)

        return results

    def similarity_search_with_score(
        self, query: str, k: int = 5
    ) -> List[tuple[Document, float]]:
        """Search with similarity scores"""
        vector_store = self.get_vector_store()
        return vector_store.similarity_search_with_score(query, k=k)

    def delete_collection(self):
        """Delete the entire collection"""
        if self._vector_store:
            self._vector_store.delete_collection()
            self._vector_store = None
            print(f"Deleted collection: {self.collection_name}")

    def get_collection_count(self) -> int:
        """Get number of documents in collection"""
        vector_store = self.get_vector_store()
        return vector_store._collection.count()

    def _filter_duplicates(self, documents: List[Document]) -> List[Document]:
        """Filter out documents that already exist based on URL"""
        unique_docs = []
        seen_urls = set()

        for doc in documents:
            url = doc.metadata.get("url", "")
            if url and url not in seen_urls:
                seen_urls.add(url)
                unique_docs.append(doc)

        return unique_docs


vector_store_manager = VectorStoreManager()
