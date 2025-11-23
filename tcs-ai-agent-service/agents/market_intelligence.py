from typing import List, Dict, Any
from langchain.chains import RetrievalQA
from langchain.prompts import PromptTemplate
from langchain_google_genai import ChatGoogleGenerativeAI
from rag.vector_store import vector_store_manager
from config import settings


class MarketIntelligenceAgent:
    """AI Agent for market intelligence and news analysis"""

    def __init__(self):
        self.llm = ChatGoogleGenerativeAI(
            model=settings.gemini_model,
            temperature=settings.gemini_temperature,
            max_tokens=settings.gemini_max_tokens,
            google_api_key=settings.gemini_api_key,
        )

        self.prompt_template = """You are a professional financial market analyst assistant. Based on the following news content, answer the user's question.

Context News:
{context}

User Question: {question}

Answer Requirements:
1. Answer based on the provided news content
2. If there is no relevant information in the news, state it clearly
3. Cite specific news sources
4. Use concise, professional language suitable for traders
5. Answer in Chinese

Answer:"""

        self.prompt = PromptTemplate(
            template=self.prompt_template, input_variables=["context", "question"]
        )

        self.qa_chain = None

    def _get_qa_chain(self) -> RetrievalQA:
        """Get or create the QA chain"""
        if self.qa_chain is None:
            vector_store = vector_store_manager.get_vector_store()
            retriever = vector_store.as_retriever(search_kwargs={"k": 5})

            self.qa_chain = RetrievalQA.from_chain_type(
                llm=self.llm,
                chain_type="stuff",
                retriever=retriever,
                return_source_documents=True,
                chain_type_kwargs={"prompt": self.prompt},
            )

        return self.qa_chain

    def query(self, question: str) -> Dict[str, Any]:
        """
        Query the agent with a question

        Args:
            question: User question

        Returns:
            Dictionary with answer and sources
        """
        try:
            qa_chain = self._get_qa_chain()
            result = qa_chain.invoke({"query": question})

            # Extract source information
            sources = self._extract_sources(result.get("source_documents", []))

            return {
                "answer": result.get("result", ""),
                "sources": sources,
                "success": True,
            }

        except Exception as e:
            print(f"Error in query: {e}")
            return {
                "answer": f"Sorry, an error occurred while processing your query: {str(e)}",
                "sources": [],
                "success": False,
                "error": str(e),
            }

    def _extract_sources(self, source_docs: List) -> List[Dict[str, Any]]:
        """Extract source information from documents"""
        sources = []

        for doc in source_docs:
            metadata = doc.metadata
            source = {
                "headline": metadata.get("headline", ""),
                "source": metadata.get("source", "Unknown"),
                "url": metadata.get("url", ""),
                "date": metadata.get("date", ""),
                "symbol": metadata.get("symbol", ""),
            }
            sources.append(source)

        return sources

    def get_daily_summary(self) -> Dict[str, Any]:
        """Get daily market summary"""
        return self.query("Summarize today's market news, focusing on major stock market dynamics")

    def get_symbol_analysis(self, symbol: str) -> Dict[str, Any]:
        """Get analysis for a specific symbol"""
        return self.query(f"Analyze recent news and market dynamics for {symbol}")


market_intelligence_agent = MarketIntelligenceAgent()
