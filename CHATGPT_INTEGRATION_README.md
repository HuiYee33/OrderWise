# ChatGPT Integration for AdminDashboardScreen

This guide explains how to set up and use the ChatGPT integration in your OrderWise2 Android app.

## Overview

The ChatGPT integration adds an AI assistant to the Admin Dashboard that can:
- Generate insights from dashboard data
- Provide restaurant management advice
- Answer questions about business operations
- Help with menu optimization and cost management

## Setup Instructions

### 1. Get OpenAI API Key

1. Go to [OpenAI Platform](https://platform.openai.com/)
2. Sign up or log in to your account
3. Navigate to "API Keys" section
4. Create a new API key
5. Copy the API key (it starts with `sk-`)

### 2. Configure API Key

Open `app/src/main/java/com/example/orderwise2/ui/ChatGPTService.kt` and replace the placeholder:

```kotlin
private val apiKey = "YOUR_OPENAI_API_KEY" // Replace with your actual API key
```

With your actual API key:

```kotlin
private val apiKey = "sk-your-actual-api-key-here"
```

### 3. Add Internet Permission

Make sure your `AndroidManifest.xml` includes internet permission:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

### 4. Build and Run

The dependencies have already been added to `build.gradle.kts`. Simply build and run your app.

## Features

### Dashboard Integration

The Admin Dashboard now has two tabs:
- **Analytics**: Original dashboard with charts and statistics
- **AI Assistant**: ChatGPT-powered assistant

### AI Assistant Features

#### Quick Actions
- **Generate Insights**: Analyzes your dashboard data and provides business insights
- **Menu Advice**: Provides recommendations for menu optimization
- **Customer Service**: Offers customer service improvement tips
- **Cost Management**: Suggests cost management strategies

#### Chat Interface
- Type any question about restaurant management
- Get real-time AI responses
- View chat history
- Loading indicators during API calls

### Example Prompts

Try asking the AI assistant questions like:
- "How can I increase my restaurant's revenue?"
- "What are the best practices for inventory management?"
- "How should I price my menu items?"
- "What marketing strategies work best for restaurants?"
- "How can I improve customer satisfaction?"
- "What are the most profitable menu items to add?"
- "How can I reduce food waste in my restaurant?"
- "What are effective ways to handle customer complaints?"
- "How should I train my staff for better service?"
- "What are the latest trends in restaurant technology?"

## Code Structure

### Files Added/Modified

1. **ChatGPTService.kt** - Handles API communication with OpenAI
2. **ChatGPTComponent.kt** - UI components for the chat interface
3. **AdminDashboardScreen.kt** - Updated with tab navigation and AI integration

### Key Components

#### ChatGPTService
- Manages HTTP requests to OpenAI API
- Handles authentication and error handling
- Provides helper methods for common tasks

#### ChatGPTComponent
- Chat interface with message history
- Quick action buttons for common queries
- Loading states and error handling

#### DashboardData
- Data class that passes dashboard statistics to the AI
- Enables context-aware responses

## Security Considerations

### API Key Security
- **Never commit your API key to version control**
- Consider using environment variables or secure storage
- For production, implement proper key management

### Rate Limiting
- OpenAI has rate limits on API calls
- The service includes timeout handling
- Consider implementing request caching for production

## Troubleshooting

### Common Issues

1. **"API call failed" error**
   - Check your API key is correct
   - Verify internet connection
   - Ensure you have sufficient OpenAI credits

2. **App crashes on startup**
   - Check all dependencies are properly synced
   - Verify internet permission is added
   - Check logcat for specific error messages

3. **Chat not responding**
   - Check network connectivity
   - Verify API key is valid
   - Check OpenAI account status

### Debug Mode

The service includes logging for debugging. Check logcat for:
- API request/response logs
- Error messages
- Network connectivity issues

## Cost Considerations

- OpenAI charges per API call
- GPT-3.5-turbo is used (cost-effective option)
- Monitor usage in your OpenAI dashboard
- Consider implementing usage limits for production

## Future Enhancements

Potential improvements:
- Message persistence using local storage
- Offline mode with cached responses
- Custom training for restaurant-specific advice
- Integration with other business metrics
- Multi-language support

## Support

For issues with:
- **OpenAI API**: Check [OpenAI Documentation](https://platform.openai.com/docs)
- **Android/Compose**: Check [Android Developer Documentation](https://developer.android.com/)
- **Project-specific**: Check the code comments and error logs

## License

This integration uses OpenAI's API. Please review OpenAI's terms of service and pricing before using in production. 